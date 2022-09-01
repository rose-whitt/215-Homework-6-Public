package main.rice.parse;

import java.util.*;
import main.rice.node.APyNode;

public class ConfigFile {
    //      FIELDS

    /**
     * field to hold name of function under test
     */
    private String testFuncName;

    /**
     * field to hold a list of Python nodes to serve as generators for test cases for the function under test
     */
    private  List<APyNode<?>> pyNodeGens;

    /**
     * the number of random test cases to be generated
     */
    private int numRandTests;


    /**
     * Constructor for a ConfigFile object, which takes in three pieces of data.
     *
     * @param funcName : name of the function under test
     * @param nodes : a list of Python nodes to serve as generators for test cases for the function under test
     * @param numRand : the number of random test cases to be generated
     */
    public ConfigFile(String funcName, List<APyNode<?>> nodes, int numRand) {
        this.testFuncName = funcName;
        this.pyNodeGens = nodes;
        this.numRandTests = numRand;

    }

    /**
     *
     * @return : name of the function under test
     */
    public String getFuncName() {
        return this.testFuncName;
    }

    /**
     * Returns the list of Python nodes which will serve as generators of test cases for the function under test.
     *
     * @return : list of Python nodes
     */
    public List<APyNode<?>> getNodes() {
        return this.pyNodeGens;
    }

    /**
     * Returns the number of random test cases to be generated.
     *
     * @return : number of test cases to be generated
     */
    public int getNumRand() {
        return this.numRandTests;
    }

}
