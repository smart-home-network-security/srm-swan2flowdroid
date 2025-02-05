package swan2flowdroid;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


public class Swan2Flowdroid {

    public static void main(String args[]) {

        /* Configure command line arguments */
        Options options = new Options();
        // Help message
        String helpOptionChar = "h";
        options.addOption(helpOptionChar, "help", false, "Print help message");
        // Input file
        String inputOptionChar = "i";
        Option inputOption = new Option(inputOptionChar, "input", true, "Input file path");
        inputOption.setRequired(true);
        options.addOption(inputOption);
        // Output file
        String outputOptionChar = "o";
        Option outputOption = new Option(outputOptionChar, "output", true, "Output file path");
        options.addOption(outputOption);

        String inFilePath = null;
        String outFilePath = null;

        /* Parse command line arguments */
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmd = parser.parse(options, args);
            
            // Print help message
            if (cmd.hasOption(helpOptionChar)) {
                formatter.printHelp("Swan2Flowdroid", options);
                System.exit(0);
            }

            inFilePath = cmd.getOptionValue(inputOptionChar);

            // Set output file path, if not provided
            if (cmd.hasOption(outputOptionChar)) {
                outFilePath = cmd.getOptionValue(outputOptionChar);
            } else {
                outFilePath = String.format("%s.flowdroid.txt", inFilePath.split("\\.")[0]);
            }

        } catch (MissingOptionException e) {
            System.err.println("Missing required parameter -i/--input");
            formatter.printHelp("Swan2Flowdroid", options);
            System.exit(1);
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            formatter.printHelp("Swan2Flowdroid", options);
            System.exit(1);
        }
        
        /* Read SWAN JSON file */
        JSONArray methods = null;
        try {
            String content = new String(Files.readAllBytes(Paths.get(inFilePath)));
            JSONObject json = new JSONObject(content);
            methods = json.getJSONArray("methods");
        } catch (IOException | JSONException e) {
            System.err.println(String.format("Error reading SWAN file %s: %s", inFilePath, e.getMessage()));
            System.exit(1);
        }

        /* Open output FlowDroid file */
        File outputFile = new File(outFilePath);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            System.err.println(String.format("Error opening output file %s: %s", outFilePath, e.getMessage()));
            System.exit(1);
        }

        /* Read all JSON SRMs
         * and convert them to FlowDroid SRMs. */
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Srm srm = null;
        for (int i = 0; i < methods.length(); i++) {
            
            /* Read JSON SRM to custom object */
            try {
                JSONObject method = methods.getJSONObject(i);
                srm = objectMapper.readValue(method.toString(), Srm.class);
            } catch (JSONException | JsonProcessingException e) {
                System.err.println(String.format("Error reading SWAN file %s: %s", inFilePath, e.getMessage()));
                continue;
            }

            /* Skip SRM if not source or sink */
            if (!(srm.isSource() || srm.isSink())) {
                continue;
            }
            
            /* Write SRM to FlowDroid format */
            try {
                writer.write(srm.toFlowdroid());
                writer.newLine();
            } catch (IOException e) {
                System.err.println(String.format("Error writing to output file %s: %s", outFilePath, e.getMessage()));
                continue;
            }

        }

        /* Close output file */
        try {
            writer.close();
        } catch (IOException e) {
            System.err.println(String.format("Error closing output file %s: %s", outFilePath, e.getMessage()));
            System.exit(1);
        }

    }

}
