package music;

import javax.sound.midi.*;

/**
 * Sends real-time MIDI to external port (e.g. loopMIDI Port 2)
 */
public class MidiOutManager {
    private static MidiOutManager instance;
    private Receiver receiver;
    private final String connectedDeviceName = "loopMIDI Port 2";

    private MidiOutManager() {
        try {
            MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
            for (MidiDevice.Info info : infos) {
                if (info.getName().equalsIgnoreCase(connectedDeviceName)) {
                    MidiDevice device = MidiSystem.getMidiDevice(info);
                    device.open();
                    receiver = device.getReceiver();
                    System.out.println("Connected to " + info.getName());
                    return;
                }
            }

            System.err.println("" + connectedDeviceName + " not found. Using default MIDI receiver.");
            receiver = MidiSystem.getReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MidiOutManager getInstance() {
        if (instance == null)
            instance = new MidiOutManager();
        return instance;
    }

    public void sendNoteOn(int channel, int note, int velocity) {
        sendShortMessage(ShortMessage.NOTE_ON, channel, note, velocity);
    }

    public void sendNoteOff(int channel, int note, int velocity) {
        sendShortMessage(ShortMessage.NOTE_OFF, channel, note, velocity);
    }

    private void sendShortMessage(int command, int channel, int data1, int data2) {
        try {
            ShortMessage msg = new ShortMessage();
            msg.setMessage(command, channel, data1, data2);
            receiver.send(msg, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
