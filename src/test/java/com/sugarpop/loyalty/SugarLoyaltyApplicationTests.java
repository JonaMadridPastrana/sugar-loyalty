package com.sugarpop.loyalty;

import com.google.api.services.walletobjects.model.LoyaltyClass;
import com.sugarpop.loyalty.service.GoogleWalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SugarLoyaltyApplicationTests {

    @Autowired
    private GoogleWalletService googleWalletService;

    @Test
    void verificarEstadoGoogleWallet() {
        LoyaltyClass lc = googleWalletService.consultarClaseLealtad();
        if (lc != null) {
            System.out.println("==================================================");
            System.out.println("GOOGLE WALLET CLASS ID: " + lc.getId());
            System.out.println("ISSUER NAME: " + lc.getIssuerName());
            System.out.println("PROGRAM NAME: " + lc.getProgramName());
            System.out.println("REVIEW STATUS EN GOOGLE: " + lc.getReviewStatus());
            System.out.println("==================================================");
        } else {
            System.out.println("No se pudo obtener la clase de Google Wallet.");
        }
    }

}
