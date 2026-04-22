package com.talktrip.talktrip.domain.order.controller;

import com.talktrip.talktrip.domain.messaging.avro.KafkaEventProducer;
import com.talktrip.talktrip.domain.messaging.dto.order.PaymentSuccessEventDTO;
import com.talktrip.talktrip.domain.order.entity.Order;
import com.talktrip.talktrip.domain.order.repository.OrderRepository;
import com.talktrip.talktrip.domain.order.service.PaymentSuccessStreamProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Tag(name = "Order", description = "주문 관련 API")
@Controller
@RequestMapping("/api/tosspay")
public class WidgetController {

    @Value("${toss.secretKey}")
    private String widgetSecretKey;

    private final OrderRepository orderRepository;
    private final PaymentSuccessStreamProducer paymentSuccessStreamProducer;
    private final KafkaEventProducer kafkaEventProducer;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public WidgetController(OrderRepository orderRepository,
                            PaymentSuccessStreamProducer paymentSuccessStreamProducer,
                            KafkaEventProducer kafkaEventProducer) {
        this.orderRepository = orderRepository;
        this.paymentSuccessStreamProducer = paymentSuccessStreamProducer;
        this.kafkaEventProducer = kafkaEventProducer;
    }

    @Operation(summary = "결제 진행", description = "주문 정보와 결제 정보를 입력받아 결제를 진행하고, 결제 여부를 반환합니다.")
    @PostMapping(value = "/confirm")
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody String jsonBody) throws Exception {

        JSONParser parser = new JSONParser();

        String orderId;
        String amount;
        String paymentKey;

        try {
            JSONObject requestData = (JSONObject) parser.parse(jsonBody);
            paymentKey = (String) requestData.get("paymentKey");
            orderId = (String) requestData.get("orderId");
            amount = (String) requestData.get("amount");
        } catch (ParseException e) {
            throw new RuntimeException("JSON 파싱 오류", e);
        }

        JSONObject requestJson = new JSONObject();
        requestJson.put("orderId", orderId);
        requestJson.put("amount", amount);
        requestJson.put("paymentKey", paymentKey);

        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorization = "Basic " + new String(encodedBytes);

        URL url = URI.create("https://api.tosspayments.com/v1/payments/confirm").toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorization);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(requestJson.toString().getBytes(StandardCharsets.UTF_8));

        int code = connection.getResponseCode();
        boolean isSuccess = code == 200;

        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();
        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
        JSONObject responseJson = (JSONObject) parser.parse(reader);
        responseStream.close();

        if (isSuccess) {
            Optional<Order> optionalOrder = orderRepository.findByOrderCode(orderId);
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                // 결제 성공 처리는 Redis Stream 워커(PaymentSuccessStreamWorker)가 담당
                paymentSuccessStreamProducer.enqueuePaymentSuccess(orderId, paymentKey, responseJson);

                // 결제 성공 이메일 트리거를 위한 Kafka 이벤트를 "여기서" 즉시 발행한다.
                // (중복 이메일 방지를 위해, 워커/OrderService 쪽에서는 payment-success Kafka 발행을 하지 않도록 분리한다.)
                PaymentSuccessEventDTO dto = PaymentSuccessEventDTO.builder()
                        .orderId(order.getId())
                        .orderCode(order.getOrderCode())
                        .memberEmail(order.getMember() != null ? order.getMember().getAccountEmail() : null)
                        .paymentKey(paymentKey)
                        .method((String) responseJson.get("method"))
                        .status((String) responseJson.get("status"))
                        .receiptUrl((String) responseJson.get("receiptUrl"))
                        .build();
                kafkaEventProducer.publishPaymentSuccess(dto);
            } else {
                logger.warn("주문 ID를 찾을 수 없습니다: {}", orderId);
            }
        }

        return ResponseEntity.status(code).body(responseJson);
    }

    @RequestMapping(value = "/success", method = RequestMethod.GET)
    public String paymentRequest(HttpServletRequest request, Model model) throws Exception {
        return "/success";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(HttpServletRequest request, Model model) throws Exception {
        return "/checkout";
    }

    @RequestMapping(value = "/fail", method = RequestMethod.GET)
    public String failPayment(HttpServletRequest request, Model model) throws Exception {
        String failCode = request.getParameter("code");
        String failMessage = request.getParameter("message");

        model.addAttribute("code", failCode);
        model.addAttribute("message", failMessage);

        return "/fail";
    }
}
