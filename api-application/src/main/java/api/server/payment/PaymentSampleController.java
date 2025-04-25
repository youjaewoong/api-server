package api.server.payment;


import api.server.payment.request.PaymentSampleRequest;
import api.server.payment.response.PaymentSampleResponse;
import api.server.payment.service.PaymentSampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sample/payments")
public class PaymentSampleController {

    private final PaymentSampleService service;

    @PostMapping
    public PaymentSampleResponse create(@RequestBody PaymentSampleRequest dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public PaymentSampleResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @GetMapping
    public List<PaymentSampleResponse> getAll() {
        return service.getAll();
    }

    @PutMapping("/{id}/amount")
    public void updateAmount(@PathVariable String id, @RequestParam Long newAmount) {
        service.updateAmount(id, newAmount);
    }

    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable String id) {
        service.cancel(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
