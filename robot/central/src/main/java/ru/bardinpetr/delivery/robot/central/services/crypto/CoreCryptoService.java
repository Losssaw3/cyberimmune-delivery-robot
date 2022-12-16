package ru.bardinpetr.delivery.robot.central.services.crypto;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.libs.crypto.AESCryptoService;
import ru.bardinpetr.delivery.libs.crypto.SignatureCryptoService;
import ru.bardinpetr.delivery.libs.messages.msg.ccu.DeliveryTask;
import ru.bardinpetr.delivery.libs.messages.msg.ccu.InputDeliveryTask;

@Slf4j
public class CoreCryptoService {
    private final SignatureCryptoService signatureCryptoService;
    private final AESCryptoService aesCryptoService;


    public CoreCryptoService(SignatureCryptoService signatureCryptoService, AESCryptoService aesCryptoService) {
        this.signatureCryptoService = signatureCryptoService;
        this.aesCryptoService = aesCryptoService;
    }

    /**
     * Verify digital signature of a task and decrypt PIN.
     *
     * @param task input signed task
     * @return decoded task of null if any errors
     */
    public DeliveryTask decodeTask(InputDeliveryTask task) {
        var data = task.getTask();

        try {
            if (!this.signatureCryptoService.verify(
                    data.toSignString(),
                    task.getSignature()
            ))
                return null;

            var pin = this.aesCryptoService.decrypt(data.getPin());

            return new DeliveryTask(data.getPosition(), pin);

        } catch (Exception ex) {
            log.error("Failed to decrypt task {}: {}", task, ex);
        }
        return null;
    }
}
