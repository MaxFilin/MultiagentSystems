import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class AverageAgent extends Agent {

    // Agent value (also used for average calculation)
    private int value;

    // Number of agents in network
    private int numberOfAgents;

    // Neighbors agents names
    private HashSet<String> neighbors;

    // Table to store values of all agents
    private HashMap<String, Integer> agentsValues;

    @Override
    protected void setup() {

        // Debug: agent creation start
        // System.out.println("creating " + getAID().getLocalName());

        neighbors = new HashSet<>();
        agentsValues = new HashMap<>();

        parseArguments(getArguments());

        agentsValues.put(getAID().getLocalName(), value);

        addBehaviour(new SendValuesOneShotBehaviour());
        addBehaviour(new ReceiveNeighborsValuesBehaviour());

        // Debug: agent creation end
        // System.out.println(getAID().getLocalName() + " created");
    }

    private void parseArguments(Object[] arguments) {
        value = (int) arguments[0];
        System.out.println(getAID().getLocalName() + " got value = " + value);

        numberOfAgents = (int) arguments[1];

        for (int i = 2; i < arguments.length; i++) {
            neighbors.add((String) arguments[i]);
        }
        System.out.println(getAID().getLocalName() + " got neighbors = " + neighbors);
    }

    public class ReceiveNeighborsValuesBehaviour extends Behaviour {

        @Override
        public void action() {
            ACLMessage message = myAgent.receive();

            if (message != null) {
                String content = message.getContent();

                HashMap<String, Integer> newAgentsValues = parseContent(content);
                agentsValues.putAll(newAgentsValues);

                System.out.println(getAID().getLocalName() + " received message with the following content: " + content);

                if (agentsValues.size() == numberOfAgents) {
                    myAgent.addBehaviour(new CalculateAverageOneShotBehaviour());
                } else {
                    myAgent.addBehaviour(new SendValuesOneShotBehaviour());
                }
            } else {
                block();
            }
        }

        private HashMap<String, Integer> parseContent(String content) {
            HashMap<String, Integer> agentsValues = new HashMap<>();

            String[] contentSplit = content.split(" ");
            for (int i = 0; i < contentSplit.length; i += 2) {
                String neighborName = contentSplit[i];
                Integer neighborValue = Integer.parseInt(contentSplit[i + 1]);

                agentsValues.put(neighborName, neighborValue);
            }

            return agentsValues;
        }

        @Override
        public boolean done() {
            return agentsValues.size() == numberOfAgents;
        }
    }

    public class SendValuesOneShotBehaviour extends OneShotBehaviour {

        @Override
        public void action() {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);

            for (String neighborName : neighbors) {
                message.addReceiver(new AID(neighborName, AID.ISLOCALNAME));
            }

            StringBuilder messageContentBuilder = new StringBuilder();

            for (Map.Entry<String, Integer> entry : agentsValues.entrySet()) {
                messageContentBuilder.append(entry.getKey());
                messageContentBuilder.append(" ");
                messageContentBuilder.append(entry.getValue());
                messageContentBuilder.append(" ");
            }

            String messageContent = messageContentBuilder.toString();
            message.setContent(messageContent);

            myAgent.send(message);

            System.out.println(getAID().getLocalName() + " sent inform message with the following content: " + messageContent);
        }
    }

    public class CalculateAverageOneShotBehaviour extends OneShotBehaviour {

        @Override
        public void action() {
            int sumValue = 0;
            for (Integer value : agentsValues.values()) {
                sumValue += value;
            }

            double average = (double) sumValue / numberOfAgents;

            System.out.println(getAID().getLocalName() + " agent calculated average = " + average);
        }
    }
}
