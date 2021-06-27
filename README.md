# Automatic Clone Validation and Classification

### Introduction

In this repository you will find a paper, titled "Automated Clone Validation and Classification", as well as the code that was used during the experiments for the paper. This paper was written as part of the course "Research Project 1" in the first semester of the 2020-2021 academic year.

The goal for this research project was finding a way to automatically classify a pair of methods into one of four classes: Type-1, Type-2, Type-3, or False Positive. Precision and recall of such automatic classification varies between 50% and 80%. These types are defined based on the syntactic similarity of the two methods:
- Type-1: The program text is identical, except for differences in comments, whitespace, or new-lines.
- Type-2: The program text may additionally contain differences in identifiers and literals.
- Type-3: The program text may additionally contain inserted or deleted sub-segments, which are called "gaps".
- False Positive: No syntactic similarity.

In the paper the methods were compared using multiple types of granularity: lines, tokens, or AST-tree nodes. In order to make the AST-based comparison possible, the AST was first traversed in pre- or post-order. 

Additionally, clone validation was also attempted, without success. Clone validation entails determining whether or not a clone pair is a false positive.

### Usage as executable

Note: it is recommended to read the paper before running the tool or inspecting the code.

The specified code can be exported as a JAR file. It can be ran using three arguments:
```
java -jar cloneClassifier.jar <comparison unit> <index file> <source directory>
```

The first argument ```comparison unit``` is one of the following values:
 - ```line``` for line-based
 - ```token``` for token-based
 - ```tree_preorder``` for tree-based (pre-order traversal)
 - ```tree_postorder``` for tree-based (post-order traversal)

The second argument ```index file``` will point to an XML file. The following is an example:
```
<clones>
    <clone type="T1">
        <source file="path/to/method1.java" startline="1" endline="5"></clone>
        <source file="path/to/method2.java" startline="20" endline="100"></clone>
    </clone>
</clones>
```
In such an XML file the ```type``` attribute must be ```T1```, ```T2```, ```T3```, or ```FP```. The ```file``` attribute must point to a Java source file. ```startline``` will point to the first line of the method (including method signature) and ```endline``` must point to the last line of the method (including closing brace). All the paths specified under the ```file``` attribute are relative to the ```source directory``` program argument.

When the application is ran, a window will appear in which the two method will be visible. The lines or tokens of the methods wil be colored according to the classification of the individual elements. Green for exact matches, yellow for parameterised matches, and red for unmatched elements that are located inbetween matched elements. At the bottom of the window both the real clone type of the clone pair, as well as the predicted clone type of the clone pair will be noted. 

### Usage for development

The code is (hopefully) sufficiently documented. The following classes hold special importance:
 - ```ClonePair```: Holds information about clone pairs.
 - ```XMLCloneIndexReader```: Reads an XML file that contains clone pairs.
 - ```EnumCloneType```: encodes the three clone types, as well as an enum instance for false positives.
 - ```Method```: Contains information about a single method, provides access to the text, AST, lines, tokens, and flattened tree nodes for that method.
 - ```IMatcher``` and ```IMethodMatching```: Interfaces for classes that match two methods.
 - ```LineMatching```: matches two methods on a line-by-line basis.
 - ```TokenMatching```: matches two methods on a token-by-token basis.
 - ```TraversalTreeMatching```: matches two methods by traversing them in pre/post-order and then comparing the resulting tree nodes.
 - ```SequenceComparisonAlgos```: contains a longest-common-subsequence implementation as well as a more simplistic comparison algorithm.
 - ```Eval```: provides utilities to process a list of ```ClonePair``` instances using an ```IMatcher```. This will return accuracy metrics.
 - ```MultiClassConfusionMatrix```: returned by ```Eval```, contains classification performance metrics.
 - ```BinaryConfusionMatrix```: returned by ```MultiClassConfusionMatrix```, contains classification performance metrics.


