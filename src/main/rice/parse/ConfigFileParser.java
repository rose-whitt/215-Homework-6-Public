package main.rice.parse;

import main.rice.node.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigFileParser {
    // fields
    private String fname;
    private JSONArray types;
    private JSONArray exDom;
    private JSONArray ranDom;
    private Integer numRan;
    /**
     * Reads and returns the contents of the file located at the input filepath;
     *      throws an IOException if the file does not exist or cannot be read.
     *
     * @param filepath
     * @return
     * @throws IOException
     */
    public String readFile(String filepath) throws IOException {
        // lauryn got this too
        String contents = Files.readString(Paths.get(filepath));

        return contents;
    }

    /**
     * Recursive helper for parse method that recursively checks the grammar of the types
     * in the json file
     *
     * @param types : the current type element (ex: "list(int")
     * @return : an APyNode object representing the types in the input
     */
    public APyNode<?> typesGrammar(String types) throws InvalidConfigException {
//        Base: If a simple type (non recursive):
//        Either “int”, “float”, or “bool”:
        // if if statement true, create a node of this simple type; error message otherwise
        types = types.strip();
        if (types.equals("int")) {
            return new PyIntNode();
        } else if (types.equals("float")) {
            return new PyFloatNode();
        } else if (types.equals("bool")) {
            return new PyBoolNode();
        }
//
//
//      Base: <type> is “str” “(“ <strVal>:
        //	<strVal> is just an valid string: create a PyStringNode; error message otherwise
        if ((types.startsWith("str"))) {
            int idxParen = types.indexOf("(");
            return new PyStringNode(types.substring(idxParen + 1));
        }

//                Recursion:
//        If <iterableType> “(“ <type>
//	<iterableType>is either “list”, “tuple”, or “set”: create a node of this iterable type
        //        Return  <iteratbleType>(parse(<type>))
        if ((types.startsWith("list"))){
            if (!types.contains("(")) {
                throw new InvalidConfigException("missing parenthesis!");
            }
            int indexParen = types.indexOf("(");
            // assuming in exact format "list("    (stripping this later)
            String sub = types.substring(indexParen + 1);
            return new PyListNode<>(typesGrammar(sub));
        } else if ((types.startsWith("tuple"))) {
            if (!types.contains("(")) {
                throw new InvalidConfigException("missing parenthesis!");
            }
            int indexParen = types.indexOf("(");
            // assuming in exact format "tuple("    (stripping this later)
            return new PyTupleNode<>(typesGrammar(types.substring(indexParen + 1)));
        } else if ((types.startsWith("set"))) {
            if (!types.contains("(")) {
                throw new InvalidConfigException("missing parenthesis!");
            }
            int indexParen = types.indexOf("(");
            // assuming in exact format "set("    (stripping this later)
            return new PySetNode<>(typesGrammar(types.substring(indexParen + 1)));
        }
        //        This sets the left child

//        If “dict” “(“ <type> “:” <type>
        if ((types.startsWith("dict"))) {
//        Return PyDictNode(<type>, type>)
            // check valid dict helper
            spuriousColons(types);
            int idxParen = types.indexOf("(");
            int indexColon = types.indexOf(":");
            return new PyDictNode<>(typesGrammar(types.substring(idxParen + 1, indexColon).strip()), typesGrammar(types.substring(indexColon + 1).strip()));
//        This sets the left and right child

        }
        throw new InvalidConfigException("type is invalid");
    }

    /**
     * helper for simpleType, checks if valid int domain
     * @param domain
     * @return
     * @throws InvalidConfigException
     */
    private boolean intDom(String domain) throws InvalidConfigException {
        // Either <intVal> “~” <invVal> or <intArray>
        if (domain.contains("~")) {
            int indexCurly = domain.indexOf("~");
            // if formatted <intVal> "~" <intVal>
            if (intVal(domain.substring(0, indexCurly)) && intVal(domain.substring(indexCurly + 1))) {
                return true;
            } else {
                throw new InvalidConfigException("invalid integer exhaustive domain");
            }
        } else if (numArray(domain, "int")) {
            return true;
        } else {
            throw new InvalidConfigException("invalid invalid integer exhaustive domain");
        }
    }

    /**
     * helper for simpleType, checks if valid float domain
     * @param domain
     * @return
     * @throws InvalidConfigException
     */
    private boolean floatDom(String domain) throws InvalidConfigException {
        // Either <intVal> “~” <invVal> or <intArray>
        if (domain.contains("~")) {
            int indexCurly = domain.indexOf("~");
            // if formatted <intVal> "~" <intVal>
            if (floatVal(domain.substring(0, indexCurly)) && floatVal(domain.substring(indexCurly + 1))) {
                return true;
            } else {
                throw new InvalidConfigException("invalid float exhaustive domain");
            }
        } else if (numArray(domain, "float")) {
                return true;
        } else {
            throw new InvalidConfigException("invalid float exhaustive domain");
        }
    }

    /**
     * helper for simpleType, checks if valid float domain
     * @param domain
     * @return
     * @throws InvalidConfigException
     */
    private boolean boolDom(String domain) throws InvalidConfigException {
        // Either <intVal> “~” <invVal> or <intArray>
        if (domain.contains("~")) {
            int indexCurly = domain.indexOf("~");
            // if formatted <intVal> "~" <intVal>
            if (boolVal(domain.substring(0, indexCurly)) && boolVal(domain.substring(indexCurly + 1))) {
                return true;
            } else {
                throw new InvalidConfigException("invalid boolean exhaustive domain");
            }
        } else if (numArray(domain, "bool")) {
            return true;
        } else {
            throw new InvalidConfigException("invalid invalid boolean exhaustive domain");
        }
    }

    /**
     * helper for domainGrammar that checks if simple type int
     * @param dom : a string of possibly integers
     * @return : a boolean representing whether or not the input is a string of an integer
     */
    private boolean intVal(String dom) {
        if (dom.matches("-?[0-9]+")) {
            return true;
        }
        return false;
    }

    /**
     * helper for domainGrammar that checks if simple type float
     * @param dom : a string of possibly floats
     * @return : a boolean representing whether or not the input is a string of a float
     */
    private boolean floatVal(String dom) {
        // must be of type, 1.0, 2.0, etc
        if (dom.matches("-?[0-9]+\\.0")) {
            return true;
        }
        return false;
    }

    /**
     * helper for domainGrammar that checks if simple type boolean
     * @param dom : a string of possibly booleans
     * @return : a boolean representing whether or not the input is a string of a boolean
     */
    private boolean boolVal(String dom) {
        // must be of type, 1.0, 2.0, etc
        if (dom.matches("[01]")) {
            return true;
        }
        return false;
    }


    /**
     * helper for intDom, floatDom, boolDom
     * checks if valid numArray (ex: "[-1, 0, 1]")
     * @param dom
     * @return
     * @throws InvalidConfigException
     */
    private boolean numArray(String dom, String numVal) throws InvalidConfigException {
        // <numArray> string comprised of any valid array of <numVal>s
        // numVal for int is 1, 2, etc
        // numVal for float is 1.0, 2.0, etc
        if (dom.startsWith("[") && dom.endsWith("]")) {
            String tempDomArr = dom.substring(1, dom.length() - 1);
            String[] temp = tempDomArr.split(", ");
            if (numVal.equals("int")) {
                for (String elem : temp) {
                    // if numArray is not comprised of ints when it should be
                    if (!intVal(elem)) {
                        // throw exception
                        throw new InvalidConfigException("exhaustive domain is not of type integer");
                    }
                }
                return true;
            } else if (numVal.equals("float")) {
                for (String elem : temp) {
                    // if numArray is not comprised of floats when it should be
                    if (!floatVal(elem)) {
                        // throw exception
                        throw new InvalidConfigException("exhaustive domain is not of type float");
                    }
                }
            } else if (numVal.equals("bool")) {
                for (String elem : temp) {
                    // if numArray is not comprised of floats when it should be
                    if (!boolVal(elem)) {
                        // throw exception
                        throw new InvalidConfigException("exhaustive domain is not of type boolean");
                    }
                }
            }  else if (numVal.equals("nonNeg")) {
                for (String elem : temp) {
                    // if numArray is not comprised of floats when it should be
                    if (Integer.parseInt(elem) < 0) {
                        // throw exception
                        throw new InvalidConfigException("exhaustive domain is not of iterable type, there is a negative integer");
                    }
                }
            }
        }
        return false;
    }

    /**
     * helper for domainGrammar
     * checks if domain is an iterable domain
     * @param dom
     * @return
     * @throws InvalidConfigException
     */
    private boolean iterableDom(String dom) throws InvalidConfigException {

//        if (dom.contains("~")) {
////            if (dom.contains("(")) {
//                int indexParen = dom.indexOf("(");
//                int indexCurly = dom.indexOf("~");
//                String firstDom = dom.substring(0, indexParen).strip();
//                Integer before = Integer.parseInt(firstDom.substring(0, indexCurly));
//                Integer after = Integer.parseInt(firstDom.substring(indexCurly + 1));
//                if (before >= 0 && after >= 0) {
//                    return true;
//                }
////            } else {
//                // strip whitespace
////                dom = dom.strip();
////                int indexCurly = dom.indexOf("~");
////                Integer before = Integer.parseInt(dom.substring(0, indexCurly));
////                Integer after = Integer.parseInt(dom.substring(indexCurly + 1));
////                if (before >= 0 && after >= 0) {
////                    return true;
////                }
////            }
//
//        } else if (numArray(dom, "nonNeg")) {
//                return true;
//        } else {
//            throw new InvalidConfigException("invalid exhaustive domain of type iterable type");
//        }
//        return false;
        dom = dom.strip();
        if (dom.contains("~")) {
            int indexCurly = dom.indexOf("~");
            if (dom.contains("(")) {
                int indexParen = dom.indexOf("(");
                Integer before = Integer.parseInt(dom.substring(0, indexCurly));
                Integer after = Integer.parseInt(dom.substring(indexCurly + 1, indexParen));
                if (before >= 0 && after >= 0) {
                    return true;
                }
            } else {
                Integer before = Integer.parseInt(dom.substring(0, indexCurly));
                Integer after = Integer.parseInt(dom.substring(indexCurly + 1));
                if (before >= 0 && after >= 0) {
                    return true;
                }
            }

        } else if (numArray(dom, "nonNeg")) {
            return true;
        } else {
            throw new InvalidConfigException("invalid exhaustive domain of type iterable type");
        }
        return false;
    }

    /**
     * helper for domain grammar that parses the domain based on the grammar
     * for both ~ and [] formats
     *
     * @param dom : a string representation of the domain
     * @return : the List<Number> representation of the domain
     * @throws InvalidConfigException : throw descriptive exception if domain invalid
     */
    private List<Number> domainList(String dom) throws InvalidConfigException {
        List<Number> domList = new ArrayList<>();

        if (dom.contains("~")) {
            int indexCurly = dom.indexOf("~");
            try {
                int start = Integer.parseInt(dom.substring(0, indexCurly));
                int end = Integer.parseInt(dom.substring(indexCurly + 1));
                if (start > end) {
                    throw new InvalidConfigException("lower bound exceeds upper bound");
                }

                for (int i = start; i <= end; i++) {
                    domList.add(i);
                }
            } catch (NumberFormatException e) {
                throw new InvalidConfigException("invalid domain syntax");
            }


        } else if (dom.startsWith("[") && dom.endsWith("]")) {
            try {
                String[] temp = dom.substring(1, dom.length() - 1).split(", ");
                for (String elem : temp) {
                    int intElem = Integer.parseInt(elem);

                    domList.add(intElem);
                }
            } catch (NumberFormatException e) {
                throw new InvalidConfigException("invalid domain syntax");
            }
        } else {
            domList.add(Integer.parseInt(dom));
        }
        return domList;
    }

    /**
     * helper for domainGrammar that checks that an iterable type does not have a negative domain value
     * @param dom : List<Number> of given domain
     * @throws InvalidConfigException : throw exception if a Number is less than zero
     */
    private void iterDom(List<Number> dom) throws InvalidConfigException {
        for (Number i : dom) {
            if ((int) i < 0) {
                throw new InvalidConfigException("cannot have a negative iterable domain");
            }
        }
    }

    /**
     * recursive helper for parse that implements the grammar for the exhaustive and random domains
     *
     * @param domain : string representation of domain
     * @param typesNodes : the current APyNode representation of the domain
     * @return : return the updated APyNode based on grammar, will now have domains applied to nodes and children
     * @throws InvalidConfigException : throw descriptive exception if there is an invalid grammar
     */
    public APyNode<?> domainGrammar(String domain, APyNode<?> typesNodes, String domainType) throws InvalidConfigException {
//        Base: If <simpleDom>
        //        Either <intDom>, <floatDom>, or <boolDom>
//        if (intVal(domain)) {
//            List<Number> tempInt = new ArrayList<>();
//            tempInt.add(Integer.parseInt(domain));
//            typesNodes.getLeftChild().setExDomain(tempInt);
//        }
        //
        // Base: if of simpleType
        spuriousParen(domain);
        if (typesNodes instanceof PyFloatNode || typesNodes instanceof PyBoolNode || typesNodes instanceof PyIntNode) {
            if (domain.contains(".") && typesNodes instanceof PyIntNode) {
                throw new InvalidConfigException("invalid domain type");
            }
            if (domain.contains(".") && domain.contains("~")) {
                throw new InvalidConfigException("invalid domain type");
            }
            List<Number> domList = domainList(domain);
            if (typesNodes instanceof PyIntNode) {
                for (Number num : domList) {
                    if (!intVal(num.toString())) {
                        throw new InvalidConfigException("invalid domain type");
                    }
                }
                if (domainType.equals("exhaustive")) {
                    typesNodes.setExDomain(domainList(domain));
                } else if (domainType.equals("random")) {
                    typesNodes.setRanDomain(domainList(domain));
                }

                return typesNodes;
                // boolean true shows that a domain was able to be set
//                return true;
            } else if (typesNodes instanceof PyFloatNode) {
                List<Number> tempFloatDom = new ArrayList<>();
                for (Number num : domList) {
                    tempFloatDom.add(num.doubleValue());
                }
                domList = tempFloatDom;
                for (Number num : domList) {
                    if (!floatVal(num.toString())) {
                        throw new InvalidConfigException("invalid domain type");
                    }
                }

                if (domainType.equals("exhaustive")) {
                    typesNodes.setExDomain(domList);
                } else if (domainType.equals("random")) {
                    typesNodes.setRanDomain(domList);
                }
                return typesNodes;
            } else if (typesNodes instanceof PyBoolNode) {
                for (Number num : domList) {
                    if (!boolVal(num.toString())) {
                        throw new InvalidConfigException("invalid domain type");
                    }
                }
                if (domainType.equals("exhaustive")) {
                    typesNodes.setExDomain(domList);
                } else if (domainType.equals("random")) {
                    typesNodes.setRanDomain(domList);
                }
                return typesNodes;
            }
//                return true;
        }

        // Base: If iterable domain
        if (typesNodes instanceof PyStringNode && iterableDom(domain)) {

            if (domainType.equals("exhaustive")) {
                typesNodes.setExDomain(domainList(domain));
            } else if (domainType.equals("random")) {
                typesNodes.setRanDomain((domainList(domain)));
            }

            return typesNodes;
        }
//
//			<intVal> string comprised of a valid integer (regex?)
//                <numArray> string comprised of any valid array of <numVal>s
//        If float type: 1.0, 2.0, etc.
//                If <intVal>: 1, 2, etc.
//                If <boolDom>
//                Either <boolVal> “~” <boolVal> or <boolArray>
//			<boolVal>: “0” or “1”
//			<boolArray>: string of any valid array of <boolVal>s
//        Base: If <iterableDom>

//        Either <nonNegIntVal> “~” <nonNegIntVal> or <nonNegIntArray>
//                If <nonNegIntVal>: >= 0
//        If <nonNegIntArray>: string comprised of any valid array of <nonNegIntVal>s
//        Recursion
        //      RECURSION

        if (domain.contains("(")) {
            int indexParen = domain.indexOf("(");
            // instance of dict
            if (typesNodes instanceof PyDictNode) {
                int idxColon = domain.indexOf(":");
                // typesNodes.setExDomain(domainList(domain.substring(0, indexParen).strip()));
                // assign dictionary's domain
                List<Number> tempDom = domainList(domain.substring(0, indexParen).strip());
                iterDom(tempDom);
                if (domainType.equals("exhaustive")) {
                    typesNodes.setExDomain(tempDom);
                    // domainGrammar(domain.substring(indexParen + 1, colon), typesNodes.getLeftChild(), domainType)
                    domainGrammar(domain.substring(indexParen + 1, idxColon).strip(), typesNodes.getLeftChild(), domainType);
                    // domainGrammar(domain.substring(colon + 1), typesNodes.getRightChild(), domainType)
                    domainGrammar(domain.substring(idxColon + 1).strip(), typesNodes.getRightChild(), domainType);
                } else if (domainType.equals("random")) {
                    typesNodes.setRanDomain(tempDom);
                    // domainGrammar(domain.substring(indexParen + 1, colon), typesNodes.getLeftChild(), domainType)
                    domainGrammar(domain.substring(indexParen + 1, idxColon).strip(), typesNodes.getLeftChild(), domainType);
                    // domainGrammar(domain.substring(colon + 1), typesNodes.getRightChild(), domainType)
                    domainGrammar(domain.substring(idxColon + 1).strip(), typesNodes.getRightChild(), domainType);
                }
                // domainGrammar(domain.substring(indexParen + 1, colon), typesNodes.getLeftChild(), domainType)
                // domainGrammar(domain.substring(colon + 1), typesNodes.getRightChild(), domainType)
            } else {
                List<Number> tempDom = domainList(domain.substring(0, indexParen).strip());
                iterDom(tempDom);
                if (domainType.equals("exhaustive")) {
                    typesNodes.setExDomain(tempDom);
                    domainGrammar(domain.substring(indexParen + 1).strip(), typesNodes.getLeftChild(), domainType);
                } else if (domainType.equals("random")) {
                    typesNodes.setRanDomain(tempDom);
                    domainGrammar(domain.substring(indexParen + 1).strip(), typesNodes.getLeftChild(), domainType);
                }
            }
        } else {
            throw new InvalidConfigException("missing parentheses");
        }

//
//
//        // else
//        // typesNodes.setExDomain(domainList(domain.substring(0, indexParen).strip()));
//        // domainGrammar(domain.substring(indexParen + 1), typesNodes.getLeftChild(), domainType)
//
//        if (domain.contains("(")) {
//            int indexParen = domain.indexOf("(");
////        If <iterableDom> “(“ <domain>
////            if ()
////            boolean validIter = iterableDom(domain.substring(0, indexParen));
//            // if false, not valid
//            // set my current node's domain
//            // call recursive
//            typesNodes.setExDomain(domainList(domain.substring(0, indexParen).strip()));
//            if ((domainGrammar(domain.substring(indexParen + 1), typesNodes.getLeftChild(), domainType)) != null) {
//                if (domainType.equals("exhaustive")) {
//                    typesNodes.getLeftChild().setExDomain(domainGrammar(domain.substring(indexParen + 1), typesNodes.getLeftChild(), domainType).getExDomain());
//                } else if (domainType.equals("random")) {
//                    typesNodes.getLeftChild().setRanDomain(domainGrammar(domain.substring(indexParen + 1), typesNodes.getLeftChild(), domainType).getExDomain());
//                }
//                return typesNodes;
//            }
//        } else if (!(domain.contains("("))) {
//            throw new InvalidConfigException("missing parentheses");
//        }
//
//
////                Check <iterableDom>
////                Return domainGrammar(<domain>)
//
////        If <iterableDom> “(“ <domain> “:” <domain>
//        if (domain.contains(":")) {
//            int indexParen = domain.indexOf("(");
//            boolean validIter = iterableDom(domain.substring(0, indexParen));
//            spuriousColons(domain);
//            int indexColon = domain.indexOf(":");
//            APyNode<?> key = domainGrammar(domain.substring(indexParen + 1, indexColon), typesNodes, domainType);
//            APyNode<?> value = domainGrammar(domain.substring(indexColon), typesNodes, domainType);
//            if (validIter && (key != null) && (value != null)) {
//                if (domainType.equals("exhaustive")) {
//                    typesNodes.getLeftChild().setExDomain(key.getExDomain());
//                    typesNodes.getLeftChild().setExDomain(value.getExDomain());
//                } else if (domainType.equals("random")) {
//                    typesNodes.getLeftChild().setRanDomain(key.getRanDomain());
//                    typesNodes.getLeftChild().setRanDomain(value.getRanDomain());
//                }
//                return typesNodes;
//            }
//        }
//                Check <iterableDom>
//                Return domainGrammar(<domain>) : domainGrammar(<domain>)
        return null;
    }

    /**
     * Parses the input string (contents of JSON file)
     * This should build a tree of Python nodes for each parameter, where each node's type,
     *      exhaustive domain, and random domain should be set up to reflect the contents of the config file.
     * These nodes should be stored in a list in the order that they were specified in the config file.
     *      That list, along with the parsed function name and number of random tests to generate,
     *      should be placed in a new ConfigFile object and returned.
     *
     * @param contents
     * @return
     * @throws InvalidConfigException
     */
    public ConfigFile parse(String contents) throws InvalidConfigException {
        // if the contents are not malformed, parse!
        validJSONObj(contents);

        // create json object
        JSONObject jsonObj = new JSONObject(contents);

        // test that the json keys' values are of the correct type
        if (!(jsonObj.get("fname") instanceof String)) {
            throw new InvalidConfigException("fname not a String");
        }
        if (!(jsonObj.get("types") instanceof JSONArray)) {
            throw new InvalidConfigException("types not a JSONArray");
        }
        if (!(jsonObj.get("exhaustive domain") instanceof JSONArray)) {
            throw new InvalidConfigException("exhaustive domain not a JSONArray");
        }
        if (!(jsonObj.get("random domain") instanceof JSONArray)) {
            throw new InvalidConfigException("random domain not a JSONArray");
        }
        if (!(jsonObj.get("num random") instanceof Integer)) {
            throw new InvalidConfigException("num rand not an Integer");
        }

        // assign the appropriate string, ints, and jsonArrays to appropriate fields
        this.fname = jsonObj.getString("fname");
        this.types = jsonObj.getJSONArray("types");
        this.exDom = jsonObj.getJSONArray("exhaustive domain");
        this.ranDom = jsonObj.getJSONArray("random domain");
        this.numRan = jsonObj.getInt("num random");
        checkKeyTypes(jsonObj);
        System.out.println("fname: " + this.fname);
        System.out.println("types: " + this.types);
        System.out.println("exDom: " + this.exDom);
        System.out.println("ranDom: " + this.ranDom);
        System.out.println("numRan: " + this.numRan);

        // check that domain and type arrays have same number of elements, else throw exception
        checkLengths(this.types, this.exDom);
        checkLengths(this.types, this.ranDom);

        // create return list
        List<APyNode<?>> nodeList = new ArrayList<>();
        // iterate over each element in type array and call typesGrammar helper on it
        int i = 0;
        for (Object typeObj : this.types) {
            String curType = (String) typeObj;
            APyNode<?> curTypeNode = typesGrammar(curType.strip());
            nodeList.add(curTypeNode);
            System.out.println("ehwdofjs");
            i++;
        }
        // iterate over each element in domain arrays and call domainGrammar helper on them
        int count = 0;
        for (Object exDomObj : this.exDom) {
            String curExDom = (String) exDomObj;
            domainGrammar(curExDom, nodeList.get(count), "exhaustive");
            count++;
        }
        int k = 0;
        for (Object ranDomObj : this.ranDom) {
            String curRanDom = (String) ranDomObj;
            domainGrammar(curRanDom, nodeList.get(k), "random");
            k++;
        }
        // create and return the ConfigFile with proper nodeList generated by recursive helpers and iteration
        return new ConfigFile(this.fname, nodeList, this.numRan);
//        if (malformed(contents)) {
//            JSONObject jsonObj = new JSONObject(contents);
//            this.fname = jsonObj.getString("fname");
//            this.types = jsonObj.getJSONArray("types");
//            this.exDom = jsonObj.getJSONArray("exhaustive domain");
//            this.ranDom = jsonObj.getJSONArray("random domain");
//            this.numRan = jsonObj.getInt("num random");
//        }
//
//        // print statements
//        System.out.println("fname: " + this.fname);
//        System.out.println("types: " + this.types);
//        System.out.println("exDom: " + this.exDom);
//        System.out.println("ranDom: " + this.ranDom);
//        System.out.println("numRan: " + this.numRan);
//
//        for (Object expr : types) {
//            parseTypesExpr((String) expr);
//        }


        //      PARSING

        // Allowable Values for "types"



        // AD HOC, NOT RECURSIVE


        // throw descriptive error message
            // if dif exception thrown, convert to InvalidConfigException
            // explicitly check for issues
        // else parse
    }

    /**
     * helper for parse
     * checks if contents can create a valid JSONFile
     *
     * @param contents : string of possible jsonFile
     * @throws InvalidConfigException
     */
    private void validJSONObj(String contents) throws InvalidConfigException {
        //      begins and ends with {, }
        if (!contents.startsWith("{")) {
            throw new InvalidConfigException("A JSONObject must begin with a '{'.");
        }
        if (!contents.endsWith("}")) {
            throw new InvalidConfigException("A JSONObject must end with a '}'.");
        }
        // Checking that all five expected keys are present
        //      keys are of type String
        if (!contents.contains("fname")) {
            throw new InvalidConfigException("JSONObject key 'fname' not found.");
        }
        if (!contents.contains("types")) {
            throw new InvalidConfigException("JSONObject key 'types' not found.");
        }
        if (!contents.contains("exhaustive domain")) {
            throw new InvalidConfigException("JSONObject key 'exhaustive domain' not found.");
        }
        if (!contents.contains("random domain")) {
            throw new InvalidConfigException("JSONObject key 'random domain' not found.");
        }
        if (!contents.contains("num random")) {
            throw new InvalidConfigException("JSONObject key 'num random' not found.");
        }

    }

    /**
     * helper for parse
     * checks that the keys' values are of correct type
     * @param json : json object to check for correct type
     * @throws InvalidConfigException : throw descriptive exception if invalid
     */
    private void checkKeyTypes(JSONObject json) throws InvalidConfigException{
        if (!(json.get("fname") instanceof String)) {
            throw new InvalidConfigException("JSONObject 'fname' is not a string.");
        }
        // types, exDom, ranDOm: JSONArray of Strings
        if (!(json.get("types") instanceof JSONArray)) {
            throw new InvalidConfigException("Uh oh! JSONObject 'types' is not a JSONArray. Try again :(");
        }
        if (!(json.get("exhaustive domain") instanceof JSONArray)) {
            throw new InvalidConfigException("Uh oh! JSONObject 'exhaustive domain' is not a JSONArray. Try again :(");
        }
        if (!(json.get("random domain") instanceof JSONArray)) {
            throw new InvalidConfigException("Uh oh! JSONObject 'random domain' is not a JSONArray. Try again :(");
        }
        if (!(json.get("num random") instanceof Integer)) {
            throw new InvalidConfigException("Oh no! JSONObject 'num random' is not an Integer. Try again!");
        }
    }

    /**
     * helper for parse
     * check that lengths of two JSONArrays are equal
     * @param arr1 : first JSONArray to compare
     * @param arr2 : second JSONArray to compare
     * @throws InvalidConfigException : throw descriptive exception if not of same length
     */
    private void checkLengths(JSONArray arr1, JSONArray arr2) throws InvalidConfigException {
        if (arr1.length() != arr2.length()) {
            throw new InvalidConfigException("Your domain length does not match!");
        }
    }
//    private boolean malformed(String contents) throws InvalidConfigException{
//        // Checking that the file contains a valid JSON object
//        validJSONObj(contents);
//
//        // if contents pass all tests above, create JSONObject
//        // use org.json package
//        JSONObject jsonObj = new JSONObject(contents);
//
//        // check key types are strings
//        checkKeyTypes(jsonObj);
//
//        // (1)     EXTRACT VALUES ASSOCIATED WITH KEYS
//        // if all keys valid, extract appropriate values
//        this.fname = jsonObj.getString("fname");
//        this.types = jsonObj.getJSONArray("types");
//        this.exDom = jsonObj.getJSONArray("exhaustive domain");
//        this.ranDom = jsonObj.getJSONArray("random domain");
//        this.numRan = jsonObj.getInt("num random");
//
//        // check for matching lengths
////        if (this.types.length() != this.exDom.length()) {
////            throw new InvalidConfigException("Exhaustive Domain length is too short!");
////        }
////        if (this.types.length() != this.ranDom.length()) {
////            throw new InvalidConfigException("Random Domain length is too short!");
////        }
////        if (this.exDom.length() != this.ranDom.length()) {
////            throw new InvalidConfigException("The lengths of the domains do not match!");
////        }
//
//        checkLengths(this.types, this.exDom);
//        checkLengths(this.types, this.ranDom);
//        checkLengths(this.exDom, this.ranDom);
//
//        //      PARENTHESES
//        // check for any spurious parentheses
//
//        // check for matching number of parentheses in exDom and ranDom as types
//        for (int i = 0; i < this.types.length(); i++) {
//            int typeOccur = countOccurences(types.getString(i), '(');
//            int exDomOccur = countOccurences(exDom.getString(i), '(');
//            int ranDomOccur = countOccurences(ranDom.getString(i), '(');
//            if (typeOccur != exDomOccur) {
//                throw new InvalidConfigException("Oh no! There's a missing parenthesis in the exhaustive domain!");
//            }
//            if (typeOccur != ranDomOccur) {
//                throw new InvalidConfigException("Oh no! There's a missing parenthesis in the random domain!");
//            }
//        }
//
//        //      COLONS
//        // check for appropriate number of colons
////        spuriousColons(this.types, "types");
////        spuriousColons(this.exDom, "exDomain");
////        spuriousColons(this.ranDom, "ranDomain");
//
//        // LETS LOOK AT TYPES
//        for (Object obj : this.exDom) {
//            String temp = (String) obj;
//            if (temp.contains(".")) {
//                throw new InvalidConfigException("your exhaustive domain has a non-integer");
//            }
//        }
//
//        for (Object obj : this.ranDom) {
//            String temp = (String) obj;
//            if (temp.contains(".")) {
//                throw new InvalidConfigException("your random domain has a non-integer");
//            }
//        }
//
//        List<String> tempTypes = new ArrayList<>();
//        for (Object obj : this.types) {
//            tempTypes.add(((String) obj).strip());
//        }
//        List<String> tempExDom = new ArrayList<>();
//        for (Object obj : this.exDom) {
//            tempExDom.add(((String) obj).strip());
//        }
//        List<String> tempRanDom = new ArrayList<>();
//        for (Object obj : this.ranDom) {
//            tempRanDom.add(((String) obj).strip());
//        }
//
//        System.out.println("the types: " + tempTypes);
//
//
//
//        // iterate over types
//        int count = 0;
//        for (String typeStr : tempTypes) {
//            String tempTypeStr = typeStr.strip();
//            String tempExStr = tempExDom.get(count).strip();
//            String tempRanStr = tempRanDom.get(count).strip();
//
//            String[] parenTypeStr = tempTypeStr.split("\\(");
//            String[] parenExStr = tempExStr.split("\\(");
//            String[] parenRanStr = tempRanStr.split("\\(");
//            // check bool
//            for (String str : parenExStr) {
//
//            }
//            System.out.println(printArray(parenTypeStr));
//            System.out.println(printArray(parenExStr));
//            System.out.println(printArray(parenRanStr));
//            System.out.println();
//
//            // at parenthesis, add that type to a types list
//
//            // add to parenetheiss in ex dom and ran dom to a list for each
//            count++;
//        }
//
//
//
//        for (Object obj : this.exDom) {
//            String temp = (String) obj;
//
//            if (temp.contains("[") && temp.length() >= 3) {
//                temp = temp.substring(1, temp.length() - 1);
//                String[] testDoms = temp.split("[,]");
//                System.out.println(printArray(testDoms));
//            }
//            if (temp.contains("~")) {
//                String[] testDoms = temp.split("[~]");
//                System.out.println(printArray(testDoms));
//                if (Integer.parseInt(testDoms[0]) > Integer.parseInt(testDoms[1])) {
//                    throw new InvalidConfigException("Uh oh! A lower bound in the exDomain is greater than upper bound!");
//                }
//            }
//        }
//
//        // for each temp type
//            // check that same index in testDoms is valid
//        for (Object obj : this.ranDom) {
//            String temp = (String) obj;
//
//            if (temp.contains("[") && temp.length() >= 3) {
//                temp = temp.substring(1, temp.length() - 1);
//                String[] testDoms = temp.split("[,]");
//                System.out.println(printArray(testDoms));
//            }
//            if (temp.contains("~")) {
//                String[] testDoms = temp.split("[~]");
//                System.out.println(printArray(testDoms));
//                if (Integer.parseInt(testDoms[0]) > Integer.parseInt(testDoms[1])) {
//                    throw new InvalidConfigException("Uh oh! A lower bound in the ranDomain is greater than upper bound!");
//                }
//            }
//        }
//
//
//        // CHECK DOMAINS
//        // lower bound exceeds upper
//        // split ~
////        for (Object obj : this.exDom) {
////            String[] testRange = ((String) obj).split("~");
////            if (Integer.parseInt(testRange[0]) > Integer.parseInt(testRange[1])) {
////                throw new InvalidConfigException("Oh no! A lower bound in exDomain is greater than the upper bound! :(");
////            }
////        }
//
//        return true;
//    }

//    private String[] splitRange(String str) throws InvalidConfigException {
////        for (Object obj : arr) {
//
//        if (str.contains("[") && str.length() >= 3) {
//            str = str.substring(1, str.length() - 1);
//            String[] testDoms = str.split("[,]");
//            return testDoms;
//        }
//        if (str.contains("~")) {
//            String[] testDoms = str.split("[~]");
//            return testDoms;
////            if (Integer.parseInt(testDoms[0]) > Integer.parseInt(testDoms[1])) {
////                throw new InvalidConfigException("Uh oh! A lower bound in the exDomain is greater than upper bound!");
////            }
//        }
//        return null;
////        }
//    }

    /**
     * Helper for parse.
     * Throws an exception when the given value has spurious colons.
     *
     * @param vals : a JSONArray of String values
     * @throws InvalidConfigException : throws an exception if contiguous parentheses or leading/ending colon
     */
    private void spuriousColons(String vals) throws InvalidConfigException {
//        for (Object obj : vals) {
        if (vals.endsWith(":")) {
            throw new InvalidConfigException("Oh no! types ends with a colon!");
        }
        if (vals.startsWith(":")) {
            throw new InvalidConfigException("Oh no! types starts with a colon!");
        }
        if (vals.contains("::")) {
            throw new InvalidConfigException("Oh no! There's a spurious colon in types!");
        }
//        }
    }

    /**
     * Helper for parse.
     * Throws an exception when the given value has spurious parentheses.
     *
     * @param vals : a JSONArray of String values
     * @throws InvalidConfigException : throws an exception if contiguous parentheses or leading/ending parenthesis
     */
    private void spuriousParen(String vals) throws InvalidConfigException {
        vals = vals.strip();
        if (vals.endsWith("(")) {
            throw new InvalidConfigException("Oh no! types ends with a colon!");
        }
        if (vals.startsWith("(")) {
            throw new InvalidConfigException("Oh no! types starts with a colon!");
        }
        if (vals.contains("((")) {
            throw new InvalidConfigException("Oh no! There's a spurious colon in types!");
        }

    }

//    /**
//     * Helper for parse.
//     *
//     * @param expr
//     * @return
//     */
//    public boolean parseTypesExpr(String expr) {
//        expr = expr.strip();
//        // recursive
//        // base: simpleType
//        if (expr.equals("int") || expr.equals("float") || expr.equals("bool")) {
//            return true;
//        } else {
//
//            System.out.println("expression separated by '(': ");
//            String[] tempForTest = expr.split("\\(");
//            System.out.println(printArray(tempForTest));
//        }
//        // deliminator: "("
////        <type>         ::= <simpleType>
////                | <iterableType> "(" <type>
////                | "str" "(" <strVal>
////                | "dict" "(" <type> ":" <type>
////
////        <simpleType>   ::= "int" | "float" | "bool"
////
////        <iterableType> ::= "list" | "tuple" | "set"
////
////        <strVal>       ::= any valid string
//        return false;
//    }

//    /**
//     * Helper that prints array in "[' ', ' ', ...]" format
//      * @param arr
//     * @return
//     */
//    private String printArray(String[] arr) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("[");
//        for (String bit : arr) {
//            sb.append("'");
//            sb.append(bit);
//            sb.append("', ");
//        }
//        sb.append("]");
//        return sb.toString();
//    }

//    /**
//     *
//     * @param search : the string to search in
//     * @param theChar : the character you are searching for
//     * @return : an int representing the number of occurrences of theChar in search
//     */
//    private int countOccurences(String search, char theChar) {
//        int count = 0;
//        for (char cur : search.toCharArray()) {
//            if (cur == theChar) {
//                count++;
//            }
//        }
//        return count;
//    }
}
