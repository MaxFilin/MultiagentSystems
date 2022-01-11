import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class App {
    protected static final List<List<Integer>> graph;

    static {
        graph = Arrays.asList(
                Arrays.asList(1, 3),
                Arrays.asList(0, 2, 3),
                Arrays.asList(1, 3),
                Arrays.asList(0, 1, 2, 4),
                List.of(3)
        );
    }

    static int maxAmountOfNeighbours = 0;

    public static void main(String[] args) {
        for (int i = 0; i < graph.size(); i++) {
            if (maxAmountOfNeighbours <= graph.get(i).size()) {
                maxAmountOfNeighbours = graph.get(i).size();
            }
        }

        // Retrieve the singleton instance of the JADE Runtime
        Runtime rt = Runtime.instance();

        //Create a container to host the Default Agent
        Profile p = new ProfileImpl();

        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.MAIN_PORT, "10098");
        p.setParameter(Profile.GUI, "true");

        ContainerController cc = rt.createMainContainer(p);

        initializeNodes(cc);
    }

    private static void initializeNodes(ContainerController container) {
        ArrayList<Double> nodesValues = new ArrayList<>();

        for (int i = 0; i < graph.size(); i++) {
            List<Integer> nodeNeighbors = graph.get(i);

            String agentName = "agent-" + i;
            double agentValue = ThreadLocalRandom.current().nextInt(1, 101);
            nodesValues.add(agentValue);

            ArrayList<Object> agentArguments = new ArrayList<>(List.of(agentValue));

            for (int neighborNode : nodeNeighbors) {
                String neighborAgentName = "agent-" + neighborNode;
                agentArguments.add(neighborAgentName);
            }

            try {
                AgentController agent = container.createNewAgent(agentName, "AverageAgent", agentArguments.toArray());
                agent.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        double nodesValuesSum = 0;

        for (double nodeValue : nodesValues) {
            nodesValuesSum += nodeValue;
        }

        double averageValue = nodesValuesSum / graph.size();

        System.out.println("Agents initialized. Expected average value = " + averageValue);
    }
}