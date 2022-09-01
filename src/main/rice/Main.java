package main.rice;

import main.rice.basegen.BaseSetGenerator;
import main.rice.concisegen.ConciseSetGenerator;
import main.rice.node.APyNode;
import main.rice.parse.ConfigFile;
import main.rice.parse.ConfigFileParser;
import main.rice.parse.InvalidConfigException;
import main.rice.test.TestCase;
import main.rice.test.TestResults;
import main.rice.test.Tester;

import java.io.IOException;
import java.util.*;

public class Main {

    /**
     *
     * Using these arguments, main() should delegate to generateTests() (described below)
     *      in order to compute the concise test set and print it to the console,
     *      along with an appropriate message explaining what's being printed.
     *
     *
     * A string containing the path to the reference solution.
     * @param args : This method should expect three arguments:
     *                  1) A string containing the path to the config file.
     *                  2) A string containing the path to the directory containing the buggy implementations.
     *                  3) A string containing the path to the reference solution.
     * @throws IOException
     * @throws InvalidConfigException
     */
    public static void main(String[] args) throws IOException, InvalidConfigException, InterruptedException {
        Set<TestCase> genTestRes = generateTests(args);
        System.out.println("The Result of Generate Tests: " + genTestRes);
    }

    /**
     * Helper for main()
     *  It should utilize the components that you built in homeworks 1-6
     *       in order to perform end-to-end test case generation, returning the concise test set.
     *
     * @param args : same three arguments as main() (described above)
     * @return
     * @throws IOException
     * @throws InvalidConfigException
     */
    public static Set<TestCase> generateTests(String[] args) throws IOException, InvalidConfigException, InterruptedException {
        // variables to contain file paths
        String configFilePath = args[0];
        String buggyDirPath = args[1];
        String solutionPath = args[2];

        // create parser
        ConfigFileParser parser = new ConfigFileParser();

        // parse and store stuff
        String fileStuff = parser.readFile(configFilePath);
        ConfigFile parsedConfig = parser.parse(fileStuff);

        // get the nodes for later and function name and number random tests
        List<APyNode<?>> nodes = parsedConfig.getNodes();
        String funcName = parsedConfig.getFuncName();
        int numRanTests = parsedConfig.getNumRand();


        // make base set generator
        BaseSetGenerator baseSetGen = new BaseSetGenerator(nodes, numRanTests);

        // store the output of generating a base set
        List<TestCase> baseSet = baseSetGen.genBaseSet();

        // construct appropriate tester
        Tester test = new Tester(funcName, solutionPath, buggyDirPath, baseSet);

        // compute the expected results
        test.computeExpectedResults();

        // store results of calling runTests()
        TestResults testResults = test.runTests();

        // find the concise test set and return
        Set<TestCase> conciseTestSet = ConciseSetGenerator.setCover(testResults);
        return conciseTestSet;
    }
}
