package music;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

import util.ErrorHandler;
import util.Utilities;
import music.MidiOutManager; 


public class MusicManager {

    private static final String instrumentFileName = "resources/data/instruments.txt";
    private static MusicManager musicManager;

    public static final int NUM_INSTRUMENT = 128;
    public static final int SYNTH_CHANNEL_NO = 15;
    public static final int SYNTH_NOTE_VELOCITY = 120;
    public static final int SYNTH_INSTRUMENT = 0; // acoustic grand piano

    public static final int PEDAL_ID = 64;
    public static final int PEDAL_ON = 127;
    public static final int PEDAL_OFF = 0;

    private Sequencer sequencer;
    private Synthesizer synth;
    private MidiChannel synthChannel;
    private int synthInstrument;
    private static List<String> instrumentNames;

    public static MusicManager getInstance() {
        if (musicManager == null)
            musicManager = new MusicManager();
        return musicManager;
    }

    public static void init() {
        musicManager = new MusicManager();
        initInstrumentNames();
    }

    private static void initInstrumentNames() {
        instrumentNames = new ArrayList<String>();
        try {
            URL url = Utilities.getResourceURL(MusicManager.instrumentFileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = in.readLine()) != null && !line.trim().isEmpty()) {
                instrumentNames.add(line);
            }
            in.close();
        } catch (IOException e) {
            ErrorHandler.display("Cannot read MIDI instrument names");
        }
        while (instrumentNames.size() < MusicManager.NUM_INSTRUMENT)
            instrumentNames.add("");
    }

    public static String getInstrumentName(int id) {
        return MusicManager.instrumentNames.get(id);
    }

    private MusicManager() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();

            synth = MidiSystem.getSynthesizer();
            synth.open();

            MidiChannel[] channels = synth.getChannels();
            synthChannel = channels[channels.length - 1];
            setSynthInstrument(MusicManager.SYNTH_INSTRUMENT);

        } catch (MidiUnavailableException e) {
            ErrorHandler.display("Cannot play MIDI music");
            sequencer = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (synth != null)
            synth.close();
        if (sequencer != null)
            sequencer.close();
        super.finalize();
    }

    public void play(Sequence sequence) {
        if (sequencer == null) return;
        sequencer.stop();
        sequencer.close();
        try {
            sequencer.open();
        } catch (MidiUnavailableException e) {
            ErrorHandler.display("Cannot play MIDI music");
            return;
        }

        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(0);
            sequencer.start();
        } catch (InvalidMidiDataException e) {
            ErrorHandler.display("MIDI music data is invalid");
        }
    }

    public void stop() {
        if (sequencer == null) return;
        sequencer.stop();
    }

    /** Plays a single note and sends it to loopMIDI. */
    public void playNote(int pitch) {
        synthChannel.noteOn(pitch, MusicManager.SYNTH_NOTE_VELOCITY);

        
        MidiOutManager.getInstance().sendNoteOn(0, pitch, MusicManager.SYNTH_NOTE_VELOCITY);
    }

    /** Stops a note and sends noteOff to loopMIDI. */
    public void stopNote(int pitch) {
        synthChannel.noteOff(pitch, 127);

        
        MidiOutManager.getInstance().sendNoteOff(0, pitch, 0);
    }

    public void pedalDown() {
        synthChannel.controlChange(MusicManager.PEDAL_ID, MusicManager.PEDAL_ON);
    }

    public void pedalUp() {
        synthChannel.controlChange(MusicManager.PEDAL_ID, MusicManager.PEDAL_OFF);
    }

    public void setSynthInstrument(int synthInstrument) {
        this.synthInstrument = synthInstrument;
        synthChannel.programChange(synthInstrument);
    }

    public void decSynthInstrument() {
        if (synthInstrument > 0) {
            setSynthInstrument(synthInstrument - 1);
        } else {
            setSynthInstrument(MusicManager.NUM_INSTRUMENT - 1);
        }
    }

    public void incSynthInstrument() {
        if (synthInstrument < MusicManager.NUM_INSTRUMENT - 1) {
            setSynthInstrument(synthInstrument + 1);
        } else {
            setSynthInstrument(0);
        }
    }

    public int getSynthInstrument() {
        return synthInstrument;
    }

    public String getInstrumentName() {
        return MusicManager.getInstrumentName(synthInstrument);
    }
}
