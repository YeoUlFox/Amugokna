/*
package com.example.inha_capston.ffmpeg_support;

*/
/*
 *      _______                       _____   _____ _____
 *     |__   __|                     |  __ \ / ____|  __ \
 *        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
 *        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/
 *        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |
 *        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|
 *
 * -------------------------------------------------------------
 *
 * TarsosDSP is developed by Joren Six at IPEM, University Ghent
 *
 * -------------------------------------------------------------
 *
 *  Info: http://0110.be/tag/TarsosDSP
 *  Github: https://github.com/JorenSix/TarsosDSP
 *  Releases: http://0110.be/releases/TarsosDSP/
 *
 *  TarsosDSP includes modified source code by various authors,
 *  for credits and info, see README.
 *
 *//*


import android.content.Context;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteOrder;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.util.FFMPEGDownloader;

public class myPipeDecoder {

    private final static Logger LOG = Logger.getLogger(myPipeDecoder.class.getName());

    private final int pipeBuffer;
    private final String pipeCommand;
    private Context context;

    private boolean printErrorstream = false;

    private String decoderBinaryAbsolutePath;

    public myPipeDecoder(Context context){
        this.context = context;
        pipeBuffer = 10000;
        //pipeArgument = "-c";
        //pipeCommand = " -ss %input_seeking%  %number_of_seconds% -i \"%resource%\" -vn -ar %sample_rate% -ac %channels% -sample_fmt s16 -f s16le pipe:1";
        pipeCommand = "-version";
        //pipeCommand = " -ss %input_seeking%  %number_of_seconds% -i \"%resource%\" -vn -ar %sample_rate% -ac %channels% -sample_fmt s16 -f s16le pipe:1 out.wav";
    }

    private boolean isAvailable(String command){
        try{
            Runtime.getRuntime().exec(command + " -version");
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public myPipeDecoder(String pipeEnvironment,String pipeArgument,String pipeCommand,String pipeLogFile,int pipeBuffer){
        this.pipeCommand = pipeCommand;
        this.pipeBuffer = pipeBuffer;
    }


    public InputStream getDecodedStream(final String resource,final int targetSampleRate,final double timeOffset, double numberOfSeconds) {
        //FFmpeg fFmpeg = FFmpeg.getInstance(context);
        try {
            //fFmpeg.loadBinary(new LoadBinaryResponseHandler());

            String command = pipeCommand;
            command = command.replace("%input_seeking%",String.valueOf(timeOffset));
            //defines the number of seconds to process
            // -t 10.000 e.g. specifies to process ten seconds
            // from the specified time offset (which is often zero).
            if(numberOfSeconds>0){
                command = command.replace("%number_of_seconds%","-t " + String.valueOf(numberOfSeconds));
            } else {
                command = command.replace("%number_of_seconds%","");
            }
            command = command.replace("%resource%", resource);
            command = command.replace("%sample_rate%", String.valueOf(targetSampleRate));
            command = command.replace("%channels%","1");

            String[] commands = null;
            commands = command.split(" ");

            fFmpeg.execute(commands, new ExecuteBinaryResponseHandler(){
            });


            LOG.info("Starting piped decoding process for " + resource);
            LOG.info(" with command: " + command);

        } catch (FFmpegNotSupportedException | FFmpegCommandAlreadyRunningException e) {
            LOG.warning("IO exception while decoding audio via sub process." + e.getMessage() );
            e.printStackTrace();
        }
        return null;
    }

    public void makeDecodedFile(final String resource,final int targetSampleRate,final double timeOffset, double numberOfSeconds) {
        FFmpeg fFmpeg = FFmpeg.getInstance(context);
        try {
            fFmpeg.loadBinary(new LoadBinaryResponseHandler());

            String command = pipeCommand;
            command = command.replace("%input_seeking%",String.valueOf(timeOffset));
            //defines the number of seconds to process
            // -t 10.000 e.g. specifies to process ten seconds
            // from the specified time offset (which is often zero).
            if(numberOfSeconds>0){
                command = command.replace("%number_of_seconds%","-t " + String.valueOf(numberOfSeconds));
            } else {
                command = command.replace("%number_of_seconds%","");
            }
            command = command.replace("%resource%", resource);
            command = command.replace("%sample_rate%", String.valueOf(targetSampleRate));
            command = command.replace("%channels%","1");

            String[] commands = null;
            commands = command.split(" ");

            fFmpeg.execute(commands, new ExecuteBinaryResponseHandler(){
            });


            LOG.info("Starting piped decoding process for " + resource);
            LOG.info(" with command: " + command);

        } catch (FFmpegNotSupportedException | FFmpegCommandAlreadyRunningException e) {
            LOG.warning("IO exception while decoding audio via sub process." + e.getMessage() );
            e.printStackTrace();
        }
    }

    public void printBinaryInfo(){
        try {
            Process p = Runtime.getRuntime().exec(decoderBinaryAbsolutePath);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line = null;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            input.close();
            //int exitVal =
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    */
/**
     * Constructs the target audio format. The audio format is one channel
     * signed PCM of a given sample rate.
     *
     * @param targetSampleRate
     *            The sample rate to convert to.
     * @return The audio format after conversion.
     *//*

    public static TarsosDSPAudioFormat getTargetAudioFormat(int targetSampleRate) {
        TarsosDSPAudioFormat audioFormat = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                targetSampleRate,
                2 * 8,
                1,
                2 * 1,
                targetSampleRate,
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));
        return audioFormat;
    }


    private boolean isAndroid(){
        try {
            // This class is only available on android
            Class.forName("android.app.Activity");
            System.out.println("Running on Android!");
            return true;
        } catch(ClassNotFoundException e) {
            //the class is not found when running JVM
            return false;
        }
    }


    private class ErrorStreamGobbler extends Thread {
        private final InputStream is;
        private final Logger logger;

        private ErrorStreamGobbler(InputStream is, Logger logger) {
            this.is = is;
            this.logger = logger;
        }

        @Override
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    logger.info(line);
                }
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private class ErrorStreamStringGlobber extends Thread {
        private final InputStream is;
        private final StringBuilder sb;

        private ErrorStreamStringGlobber(InputStream is) {
            this.is = is;
            this.sb = new StringBuilder();
        }

        @Override
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        public String getErrorStreamAsString(){
            return sb.toString();
        }
    }
}*/
