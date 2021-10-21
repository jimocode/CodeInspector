package org.sec.util;

import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;
import org.sec.core.CallGraph;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;

public class DrawUtil {
    public static void drawCallGraph(Set<CallGraph> targetCalls) {
        System.out.println(targetCalls);
        try {
            List<Node> results = new ArrayList<>();
            for (CallGraph callGraph : targetCalls) {
                Node tmp = node(getClassName(callGraph.getCallerMethod().getClassReference().getName()) +
                        "\n" + callGraph.getCallerMethod().getName() +
                        "\n" + callGraph.getCallerMethod().getDesc());
                tmp = tmp.link(node(getClassName(callGraph.getTargetMethod().getClassReference().getName()) +
                        "\n" + callGraph.getTargetMethod().getName() +
                        "\n" + callGraph.getCallerMethod().getDesc()));
                results.add(tmp);
            }
            Graph g = graph("example1").directed()
                    .graphAttr().with(Rank.dir(LEFT_TO_RIGHT))
                    .linkAttr().with("class", "link-class")
                    .with(results);
            Graphviz.fromGraph(g).height(1000).render(Format.PNG).toFile(new File("example/ex1.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getClassName(String fullName) {
        String[] strings = fullName.split("/");
        return strings[strings.length - 1];
    }
}
