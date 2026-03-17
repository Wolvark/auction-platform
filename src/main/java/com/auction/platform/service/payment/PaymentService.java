package com.auction.platform.service.payment;

import com.auction.platform.model.account.PaymentMethod;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mock payment service that simulates integration with Visa, Mastercard, Google Pay, and Apple Pay.
 * In a production environment, replace the implementation with real payment gateway SDKs
 * (e.g., Stripe, Braintree, or dedicated provider SDKs).
 */
@Service
public class PaymentService {

    /**
     * Processes a deposit using the specified payment method.
     *
     * @param amount        the amount to charge
     * @param paymentMethod the payment method (VISA, MASTERCARD, GOOGLE_PAY, APPLE_PAY)
     * @return a payment reference ID on success
     * @throws PaymentException if the payment fails
     */
    public String processDeposit(BigDecimal amount, PaymentMethod paymentMethod) {
        // TODO: Integrate with real payment gateway (e.g., Stripe) for VISA, MASTERCARD,
        //       GOOGLE_PAY and APPLE_PAY. Stripe supports all four via their PaymentIntent API.
        //       Example: stripeClient.paymentIntents().create(...)
        validatePaymentMethod(paymentMethod);
        validateAmount(amount);

        // Simulate successful payment and return a mock reference ID
        return UUID.randomUUID().toString();
    }

    private void validatePaymentMethod(PaymentMethod method) {
        if (method == PaymentMethod.INTERNAL) {
            throw new PaymentException("INTERNAL payment method cannot be used for deposits");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Deposit amount must be greater than zero");
        }
    }
}
