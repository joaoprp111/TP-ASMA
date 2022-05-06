import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class MainContainer {
	Runtime rt;
	ContainerController container;
	
	public void initMainContainerInPlatform(String host, String port, String containerName) {
		// Get the JADE runtime interface (singleton)
		this.rt = Runtime.instance();
		// Create a Profile, where the launch arguments are stored
		Profile prof = new ProfileImpl();
		prof.setParameter(Profile.MAIN_HOST, host);
		prof.setParameter(Profile.MAIN_PORT, port);
		prof.setParameter(Profile.CONTAINER_NAME, containerName);
		prof.setParameter(Profile.MAIN, "true");
		prof.setParameter(Profile.GUI, "true");
		// create a main agent container
		this.container = rt.createMainContainer(prof);
		rt.setCloseVM(true);
	}
	
	public ContainerController initContainerInPlatform(String host, String port, String containerName) {
		// Get the JADE runtime interface (singleton)
		this.rt = Runtime.instance();
		// Create a Profile, where the launch arguments are stored
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, containerName);
		profile.setParameter(Profile.MAIN_HOST, host);
		profile.setParameter(Profile.MAIN_PORT, port);
		// create a non-main agent container
		ContainerController container = rt.createAgentContainer(profile);
		return container;
	}
	
	public void startAgentInPlatform(String name, String classpath, Object[] args) {
		try {
			AgentController ac = container.createNewAgent(name, classpath, args);
			ac.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void startAgentInPlatformContainer(ContainerController input_container, String name, String classpath, Object[] args) {
		try {
			AgentController ac = input_container.createNewAgent(name, classpath, args);
			ac.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		MainContainer a = new MainContainer();
		int numPlayers = 5;
		
		// Main Container Creation
		a.initMainContainerInPlatform("localhost", "9885", "MainContainer");

		// Containers Creation - Create 1 container (separated environments) inside the Main container
		Object[] args_input = new Object[] { "Manager_Container", "TeamA_Container", "TeamB_Container" };
		ContainerController newcontainer1 = a.initContainerInPlatform("localhost", "9887",
				args_input[0].toString());
		ContainerController newcontainer2 = a.initContainerInPlatform("localhost", "9888",
				args_input[1].toString());
		ContainerController newcontainer3 = a.initContainerInPlatform("localhost", "9889",
				args_input[2].toString());

		// Start Manager
		a.startAgentInPlatformContainer(newcontainer1, "Manager", "Agents.Manager", new Object[] { });
		
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Start coaches
		a.startAgentInPlatformContainer(newcontainer2, "CoachA", "Agents.Coach", new Object[] { "A", numPlayers });
		a.startAgentInPlatformContainer(newcontainer3, "CoachB", "Agents.Coach", new Object[] { "B", numPlayers });
		
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Start players
		for(int i = 0; i < numPlayers; i++) {
			String playerA = "PlayerA" + (i+1);
			String idA = "A" + (i+1);
			a.startAgentInPlatformContainer(newcontainer2, playerA, "Agents.Player", new Object[] { idA });
			String playerB = "PlayerB" + (i+1);
			String idB = "B" + (i+1);
			a.startAgentInPlatformContainer(newcontainer3, playerB, "Agents.Player", new Object[] { idB });
			//Sleep para os jogadores esperarem que o anterior já tenha informado o coach
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}