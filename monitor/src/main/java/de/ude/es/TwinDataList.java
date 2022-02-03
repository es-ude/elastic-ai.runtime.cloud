package de.ude.es;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TwinDataList {

    public void handleMessageParameter(String[] twinParameter, List<TwinData> newTwinList) {
        String twinURI = twinParameter[0];
        TwinData twin = getTwin(twinURI);
        newTwinList.add(Objects.requireNonNullElseGet(twin, () -> new TwinData("Twin " + newTwinList.size(), twinURI)));
    }

    List<TwinData> twins = new ArrayList<>();
    final Lock lock = new ReentrantLock();

    public void twinListUpdate() {
//        try {
//            lock.lock();
//            List<DigitalTwin> newTwinList = new ArrayList<>();
//            for (EIPMessage.Parameter parameter : message.parameters) {
//                ByteArrayInputStream in = new ByteArrayInputStream((byte[]) parameter.value);
//                ObjectInputStream is = new ObjectInputStream(in);
//                String[] twinParameters = (String[]) is.readObject();
//                handleMessageParameter(twinParameters, newTwinList);
//            }
//            twins = newTwinList;
//            sortTwins();
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        } finally {
//            lock.unlock();
//        }
    }

    public void changeTwinName(String twinURI, String newName) {
        try {
            lock.lock();
            TwinData twin = getTwin(twinURI);
            if (twin != null) {
                twin.setName(newName);
            }
        } finally {
            lock.unlock();
        }
    }

    public TwinData getTwin(String twinURI) {
        try {
            lock.lock();
            for (TwinData tw : twins) {
                if (Objects.equals(tw.getID(), twinURI)) {
                    return tw;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

}
