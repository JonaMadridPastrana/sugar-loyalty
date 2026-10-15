package com.sugarpop.loyalty.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.api.services.walletobjects.Walletobjects;
import com.google.api.services.walletobjects.model.LoyaltyClass;
import com.google.api.services.walletobjects.model.LoyaltyObject;
import com.google.api.services.walletobjects.model.Barcode;
import com.google.api.services.walletobjects.model.Image;
import com.google.api.services.walletobjects.model.ImageUri;
import com.google.api.services.walletobjects.model.LoyaltyPoints;
import com.google.api.services.walletobjects.model.LoyaltyPointsBalance;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;

@Service
public class GoogleWalletService {

    @Value("${google.wallet.service-account-file}")
    private Resource serviceAccountResource;

    @Value("${google.wallet.issuer-id}")
    private String issuerId;

    private Walletobjects walletService;
    private ServiceAccountCredentials serviceAccountCredentials;

    @PostConstruct
    public void init() {
        try {
            InputStream inputStream = serviceAccountResource.getInputStream();
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(List.of("https://www.googleapis.com/auth/wallet_object.issuer"));

            if (credentials instanceof ServiceAccountCredentials sac) {
                this.serviceAccountCredentials = sac;
            }

            walletService = new Walletobjects.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("SugarPop-Loyalty")
                    .build();

            System.out.println("¡Google Wallet Service inicializado correctamente para el emisor: " + issuerId + "!");

            crearClaseLealtadBase();

        } catch (Exception e) {
            System.err.println("Error al inicializar las credenciales de Google Wallet: " + e.getMessage());
        }
    }

    public void crearClaseLealtadBase() {
        String classId = issuerId.trim() + ".sugar_pop_loyalty_class";
        try {
            try {
                LoyaltyClass lc = walletService.loyaltyclass().get(classId).execute();
                System.out.println("La clase de lealtad de Sugar Pop ya existe en Google Wallet. ReviewStatus: " + lc.getReviewStatus());
                return;
            } catch (Exception e) {
                // Si no existe, se procede a crearla
            }

            LoyaltyClass loyaltyClass = new LoyaltyClass()
                    .setId(classId)
                    .setIssuerName("Sugar Pop")
                    .setProgramName("Sugar Pop Loyalty")
                    .setProgramLogo(new Image().setSourceUri(new ImageUri().setUri("https://images.unsplash.com/photo-1555507036-ab1f4038808a")))
                    .setHexBackgroundColor("#D81B60")
                    .setReviewStatus("UNDER_REVIEW"); // <- Requerido por la API de Google Wallet

            walletService.loyaltyclass().insert(loyaltyClass).execute();
            System.out.println("¡Clase de lealtad de Sugar Pop creada exitosamente en Google Wallet!");

        } catch (Exception e) {
            System.err.println("Aviso al verificar/crear la clase de lealtad: " + e.getMessage());
        }
    }

    public LoyaltyClass consultarClaseLealtad() {
        String classId = issuerId.trim() + ".sugar_pop_loyalty_class";
        try {
            return walletService.loyaltyclass().get(classId).execute();
        } catch (Exception e) {
            System.err.println("Error al consultar clase de lealtad: " + e.getMessage());
            return null;
        }
    }

    public String crearObjetoLealtadCliente(String clienteEmail, String clienteNombre, int puntosIniciales) {
        String classId = issuerId.trim() + ".sugar_pop_loyalty_class";
        String objectId = issuerId.trim() + ".object_" + System.currentTimeMillis();

        try {
            LoyaltyObject loyaltyObject = new LoyaltyObject()
                    .setId(objectId)
                    .setClassId(classId)
                    .setState("ACTIVE")
                    .setAccountName(clienteNombre)
                    .setAccountId(clienteEmail)
                    .setBarcode(new Barcode().setType("QR_CODE").setValue(clienteEmail))
                    .setLoyaltyPoints(new LoyaltyPoints()
                            .setBalance(new LoyaltyPointsBalance().setInt(puntosIniciales))
                            .setLabel("Puntos Sugar"));

            walletService.loyaltyobject().insert(loyaltyObject).execute();

            // Retornamos el enlace oficial firmado con JWT para Google Wallet
            return generarJwtSaveLink(objectId);

        } catch (Exception e) {
            System.err.println("Error al crear el objeto de Google Wallet para el cliente: " + e.getMessage());
            return null;
        }
    }

    private String generarJwtSaveLink(String objectId) {
        if (serviceAccountCredentials == null) {
            return null;
        }
        try {
            // 1. Encabezado estándar del token JWT (Algoritmo RS256)
            String header = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
            String encodedHeader = java.util.Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(header.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // 2. Contenido del pase que le pedimos a Google guardar
            long iat = java.time.Instant.now().getEpochSecond();
            String payload = String.format(
                    "{\"iss\":\"%s\",\"aud\":\"google\",\"typ\":\"savetowallet\",\"iat\":%d,\"payload\":{\"loyaltyObjects\":[{\"id\":\"%s\"}]}}",
                    serviceAccountCredentials.getClientEmail(),
                    iat,
                    objectId
            );
            String encodedPayload = java.util.Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // 3. Firma criptográfica con la llave privada de Sugar Pop
            byte[] signature = serviceAccountCredentials.sign((encodedHeader + "." + encodedPayload).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String encodedSignature = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(signature);

            // 4. Enlace oficial Save to Google Wallet
            return "https://pay.google.com/gp/v/save/" + encodedHeader + "." + encodedPayload + "." + encodedSignature;
        } catch (Exception e) {
            System.err.println("Error al generar el JWT de Google Wallet: " + e.getMessage());
            return null;
        }
    }
}
