package ru.bardinpetr.delivery.robot.central.services.crypto;

import lombok.extern.slf4j.Slf4j;
import ru.bardinpetr.delivery.common.libs.crypto.SignatureCryptoService;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.DeliveryTask;
import ru.bardinpetr.delivery.common.libs.messages.msg.ccu.InputDeliveryTask;

@Slf4j
public class CoreCryptoService {
    private final SignatureCryptoService signatureCryptoService;

    public CoreCryptoService(SignatureCryptoService signatureCryptoService) {
        this.signatureCryptoService = signatureCryptoService;
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

            return new DeliveryTask("", data.getPosition(), data.getPin());

        } catch (Exception ex) {
            log.error("Failed to decrypt task {}: {}", task, ex);
        }
        return null;
    }
}
