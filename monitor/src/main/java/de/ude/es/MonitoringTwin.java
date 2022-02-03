package de.ude.es;

import de.ude.es.twin.DigitalTwin;
import org.springframework.stereotype.Service;

@Service
public class MonitoringTwin extends DigitalTwin {

    private final TwinDataList twinDataList = new TwinDataList();

    public MonitoringTwin() {
        super("sys:/monitor");
    }


//    @EIPMessageHandler("/FlashBitFile/ack")
//    public void handleBitFileFlashingAck(EIPMessage eipMessage) {
//        String fileName = getDeviceListReference().getLastFlashedFile(eipMessage.sender);
//        if (eipMessage.type.equals(EIPMessage.UriType.ACK)) {
//            getDeviceListReference().changeLastAction(eipMessage.sender, "Successfully flashed " + fileName + ".");
//            System.out.println("100%");
//        } else if (eipMessage.type.equals(EIPMessage.UriType.DATA)) {
//            getDeviceListReference().changeLastAction(eipMessage.sender, fileName + " flashing progress: " + eipMessage.parameters[0].value);
//            System.out.println(eipMessage.parameters[0].value);
//        } else {
//            getDeviceListReference().changeLastAction(eipMessage.sender, "Flashing of " + fileName + " FAILED.");
//            System.out.println(eipMessage.sender + " not flashed");
//        }
//    }

//    public void sendFileToDevice(MultipartFile bitfile, String twinURI) {
//        try {
//            sendSystemEIPMessage(new EIPMessage(twinURI + "/FlashBitFile", getTWIN_URI(), EIPMessage.UriType.POST,
//                    EIPMessage.parameters(bitfile.getOriginalFilename(), bitfile.getBytes())));
//            System.out.println("Flashing \"" + twinURI + "\" with \"" + bitfile.getOriginalFilename() + "\"");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    public TwinDataList getDigitalTwinListReference() {
        return twinDataList;
    }

}
