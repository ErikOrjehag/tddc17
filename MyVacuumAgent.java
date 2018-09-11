package tddc17;


import aima.core.environment.liuvacuum.*;
import aima.core.util.datastructure.Pair;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

class MyAgentState
{
	public int[][] world = new int[30][30];
	public int initialized = 0;
	final int UNKNOWN 	= 0;
	final int WALL 		= 1;
	final int CLEAR 	= 2;
	final int DIRT		= 3;
	final int HOME		= 4;
	final int ACTION_NONE 			= 0;
	final int ACTION_MOVE_FORWARD 	= 1;
	final int ACTION_TURN_RIGHT 	= 2;
	final int ACTION_TURN_LEFT 		= 3;
	final int ACTION_SUCK	 		= 4;
	
	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;
	
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;
	
	MyAgentState()
	{
		for (int i=0; i < world.length; i++)
			for (int j=0; j < world[i].length ; j++)
				world[i][j] = UNKNOWN;
		world[1][1] = HOME;
		agent_last_action = ACTION_NONE;
	}
	// Based on the last action and the received percept updates the x & y agent position
	public void updatePosition(DynamicPercept p)
	{
		Boolean bump = (Boolean)p.getAttribute("bump");

		if (agent_last_action==ACTION_MOVE_FORWARD && !bump)
	    {
			switch (agent_direction) {
			case MyAgentState.NORTH:
				agent_y_position--;
				break;
			case MyAgentState.EAST:
				agent_x_position++;
				break;
			case MyAgentState.SOUTH:
				agent_y_position++;
				break;
			case MyAgentState.WEST:
				agent_x_position--;
				break;
			}
	    }
		else if (agent_last_action == ACTION_TURN_RIGHT) {
			agent_direction = (agent_direction + 1) % 4;
		}
		else if (agent_last_action == ACTION_TURN_LEFT) {
			agent_direction = (agent_direction - 1) % 4;
			if (agent_direction < 0) agent_direction += 4;
		}
		
	}
	
	public void updateWorld(int x_position, int y_position, int info)
	{
		world[x_position][y_position] = info;
	}
	
	public void printWorldDebug()
	{
		for (int i=0; i < world.length; i++)
		{
			for (int j=0; j < world[i].length ; j++)
			{
				if (world[j][i]==UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i]==WALL)
					System.out.print(" # ");
				if (world[j][i]==CLEAR)
					System.out.print(" . ");
				if (world[j][i]==DIRT)
					System.out.print(" D ");
				if (world[j][i]==HOME)
					System.out.print(" H ");
			}
			System.out.println("");
		}
	}
}

class MyAgentProgram implements AgentProgram {

	public int initnialRandomActions = 10;
	private Random random_generator = new Random();
	
	// Here you can define your variables!
	public int iterationCounter = 10;
	public MyAgentState state = new MyAgentState();
	
	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other percepts are ignored
	// returns a random action
	public Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if(action==0) {
		    state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action==1) {
			state.agent_last_action = state.ACTION_TURN_RIGHT;
		    return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		}
		state.agent_last_action=state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}
	
	
	@Override
	public Action execute(Percept percept) {
		
		// DO NOT REMOVE this if condition!!!
    	if (initnialRandomActions>0) {
    		return moveToRandomStartPosition((DynamicPercept) percept);
    	} else if (initnialRandomActions==0) {
    		// process percept for the last step of the initial random actions
    		initnialRandomActions--;
    		state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.agent_last_action=state.ACTION_SUCK;
	    	return LIUVacuumEnvironment.ACTION_SUCK;
    	}
		
    	// This example agent program will update the internal agent state while only moving forward.
    	// START HERE - code below should be modified!
    	    	
    	System.out.println("x=" + state.agent_x_position);
    	System.out.println("y=" + state.agent_y_position);
    	System.out.println("dir=" + state.agent_direction);
    	
		
	    iterationCounter--;
	    
	    if (iterationCounter==0)
	    	return NoOpAction.NO_OP;

	    DynamicPercept p = (DynamicPercept) percept;
	    Boolean bump = (Boolean)p.getAttribute("bump");
	    Boolean dirt = (Boolean)p.getAttribute("dirt");
	    Boolean home = (Boolean)p.getAttribute("home");
	    System.out.println("percept: " + p);
	    
	    // State update based on the percept value and the last action
	    state.updatePosition((DynamicPercept)percept);
	    if (bump) {
			switch (state.agent_direction) {
			case MyAgentState.NORTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position-1,state.WALL);
				break;
			case MyAgentState.EAST:
				state.updateWorld(state.agent_x_position+1,state.agent_y_position,state.WALL);
				break;
			case MyAgentState.SOUTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position+1,state.WALL);
				break;
			case MyAgentState.WEST:
				state.updateWorld(state.agent_x_position-1,state.agent_y_position,state.WALL);
				break;
			}
	    }
	    if (dirt)
	    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.DIRT);
	    else
	    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR);
	    
	    state.printWorldDebug();
	    
	    
	    // Next action selection based on the percept value
	    if (dirt)
	    {
	    	System.out.println("DIRT -> choosing SUCK action!");
	    	state.agent_last_action=state.ACTION_SUCK;
	    	return LIUVacuumEnvironment.ACTION_SUCK;
	    } 
	    else
	    {
	    	if (bump)
	    	{
	    		state.agent_last_action=state.ACTION_NONE;
		    	return NoOpAction.NO_OP;
	    	}
	    	else
	    	{
	    		state.agent_last_action=state.ACTION_MOVE_FORWARD;
	    		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	    	}
	    }
	}
}



/* # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
 * 
 * 
 * 
 *                                                  LAB 1 CODE BELLOW
 * 
 * 
 * 
 *  # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
 */


/*
 * Node class used for Breadth First Search.
 */
class Node {
	int x;
	int y;
	int dir;
	Action action;
	
	public Node(int x, int y, int dir, Action action) {
		this.x = x;
		this.y = y;
		this.dir = dir;
		this.action = action;
	}
}

/*
 * Reachable child notes of a node.
 */
class Children {
	public Node forward;
	public Node left;
	public Node right;
}

/*
 * Our lab 1 agent.
 */
public class MyVacuumAgent extends AbstractAgent {
	
    public MyVacuumAgent() {
    	
    	super(new MyAgentProgram() {
    		
    		/*
    		 * What to look for right now in the Breadth First Search.
    		 * This will start out as 0 (UNKNOWN) and switch to 4 (HOME)
    		 * when all reachable UNKNOWN cells have been explored.
    		 */
    		int target = 0;
    		
    		/*
    		 * Returns all reachable nodes one Action away from given node.
    		 * Nodes at the edge of the world and WALL nodes are not reachable,
    		 * this is indicated with null.
    		 */
    		public Children getChildren(Node node) {
    			int x = node.x;
    			int y = node.y;
    			int dir = node.dir;
    			Children children = new Children();
    			
    			// forward
    			int dx = (dir == MyAgentState.EAST ? 1 : (dir == MyAgentState.WEST ? -1 : 0));
    			int dy = (dir == MyAgentState.SOUTH ? 1 : (dir == MyAgentState.NORTH ? -1 : 0));
    			int fx = x + dx;
    			int fy = y + dy;
    			
    			int size = 15;
    			
    			if (fx >= 1 && fx <= size && fy >= 1 && fy <= size) {
    				if (state.world[fx][fy] != 1) { // not WALL
    					children.forward = new Node(fx, fy, dir, node.action);
    				}
    			}
    			
    			// turn right
    			children.right = new Node(x, y, (dir+1)%4, node.action);
    			
    			// turn left
    			int ld = (dir-1)%4;
    			if (ld < 0) ld += 4;
    			children.left = new Node(x, y, ld, node.action);
    			
				return children;
    		}
    		
    		/*
    		 * Is this node already visited?
    		 */
    		public boolean alreadyClosed(boolean[][][] closed, Node node) {
    			return closed[node.x][node.y][node.dir];
    		}
    		
    		/*
    		 * Add to nodes that have already been visited.
    		 */
    		public void addToClosed(boolean[][][] closed, Node node) {
    			closed[node.x][node.y][node.dir] = true;
    		}
    		
    		/*
    		 * Is the node already in the list of nodes to explore?
    		 */
    		public boolean alreadyOpen(boolean[][][] inOpen, Node node) {
    			return inOpen[node.x][node.y][node.dir];
    		}
    		
    		/*
    		 * Add node to list of nodes to explore.
    		 */
    		public void addToOpen(LinkedList<Node> open, boolean[][][] inOpen, Node node) {
    			open.addLast(node);
    			inOpen[node.x][node.y][node.dir] = true;
    		}
    		
    		/*
    		 * Get next node to explore if available, else null.
    		 */
    		public Node getNextOpen(LinkedList<Node> open, boolean[][][] inOpen) {
    			Node node = open.pollFirst();
    			if (node != null) {
    				inOpen[node.x][node.y][node.dir] = false;
    			}
    			return node;
    		}
    		
    		/*
    		 * Add to list of nodes to explore if it exists, has not already been visited, 
    		 * and is not already in the list to explore.
    		 */
    		public void exploreChild(boolean[][][] closed, LinkedList<Node> open, boolean[][][] inOpen, Node node) {
    			if (node != null) {
					if (!alreadyClosed(closed, node)) {
    					if (!alreadyOpen(inOpen,node)) {
    						addToOpen(open, inOpen, node);
    					}
    				}
				}
    		}
    		
    		/*
    		 * Breadth First Search to find the Action that leads us towards the 
    		 * cell of type [target] which is the fewest actions away from us.
    		 */
    		public Action bfs(int x, int y, int dir, int target) {

    			// Nodes that have been visited (  x   y  dir).
    			boolean[][][] closed = new boolean[30][30][4];
    			
    			// Nodes that we want to explore next (queue).
    			LinkedList<Node> open = new LinkedList<Node>();
    			boolean[][][] inOpen = new boolean[30][30][4];
    			
    			// Find closest children and set what action is needed to get to them.
    			Children firstChildren = getChildren(new Node(x, y, dir, NoOpAction.NO_OP));
    			if (firstChildren.forward != null) {
    				firstChildren.forward.action = LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
    				addToOpen(open, inOpen, firstChildren.forward);
        		}
    			if (firstChildren.right != null) {
    				firstChildren.right.action = LIUVacuumEnvironment.ACTION_TURN_RIGHT;
    				addToOpen(open, inOpen, firstChildren.right);
        		}
    			if (firstChildren.left != null) {
    				firstChildren.left.action = LIUVacuumEnvironment.ACTION_TURN_LEFT;
    				addToOpen(open, inOpen, firstChildren.left);
        		}
    			
    			// While there is still nodes to explore.
    			while (!open.isEmpty()) {
    				
    				// Get the next node in the front of the queue.
    				Node subtreeRoot = getNextOpen(open, inOpen);
    				
    				// If it is the [target] we are looking for...
    				if (state.world[subtreeRoot.x][subtreeRoot.y] == target) {
    					// ... then return the action required to take us one step towards it.
    					return subtreeRoot.action;
    				}
    				
    				// Add the children of [subtreeRoot] to the queue to explore. 
    				Children children = getChildren(subtreeRoot);
    				exploreChild(closed, open, inOpen, children.forward);
    				exploreChild(closed, open, inOpen, children.right);
    				exploreChild(closed, open, inOpen, children.left);
    				
    				// The [subtreeRoot] has now been visited.
    				addToClosed(closed, subtreeRoot);
    			}
    			
    			return NoOpAction.NO_OP;
    		}
    		
    		/*
    		 * Used to keep track of the last action performed.
    		 */
    		public Action doAction(Action action) {
    			if (action == LIUVacuumEnvironment.ACTION_MOVE_FORWARD) {
    				state.agent_last_action = state.ACTION_MOVE_FORWARD;
    			} else if (action == LIUVacuumEnvironment.ACTION_TURN_RIGHT) {
    				state.agent_last_action = state.ACTION_TURN_RIGHT;
    			} else if (action == LIUVacuumEnvironment.ACTION_TURN_LEFT) {
    				state.agent_last_action = state.ACTION_TURN_LEFT;
    			} else if (action == LIUVacuumEnvironment.ACTION_SUCK) {
    				state.agent_last_action = state.ACTION_SUCK;
    			}
    			return action;
    		}
    		
    		/*
    		 * Used to update world state known to the robot based on perception and
    		 * previous robot actions.
    		 */
    		public void updateKnownState(DynamicPercept p) {

    		    state.updatePosition(p);
    		    
    			Boolean bump = (Boolean)p.getAttribute("bump");
    			Boolean home = (Boolean)p.getAttribute("home");
    		    
    		    if (bump) {
    				switch (state.agent_direction) {
    				case MyAgentState.NORTH:
    					state.updateWorld(state.agent_x_position, state.agent_y_position-1, state.WALL);
    					break;
    				case MyAgentState.EAST:
    					state.updateWorld(state.agent_x_position + 1, state.agent_y_position, state.WALL);
    					break;
    				case MyAgentState.SOUTH:
    					state.updateWorld(state.agent_x_position, state.agent_y_position+1, state.WALL);
    					break;
    				case MyAgentState.WEST:
    					state.updateWorld(state.agent_x_position - 1, state.agent_y_position, state.WALL);
    					break;
    				}
    		    }
    		    
    		    if (!home) {
    		    	state.updateWorld(state.agent_x_position, state.agent_y_position, state.CLEAR);
    		    }
    		}
    		
    		/*
    		 * Perform one step of AI goodness.
    		 */
    		public Action execute(Percept percept) {
    			
    			DynamicPercept p = (DynamicPercept) percept;
    			
    			// Move to random position initially.
    	    	if (initnialRandomActions > 0) {
    	    		return moveToRandomStartPosition(p);
    	    		
	    		} else if (initnialRandomActions == 0) {
	        		// process percept for the last step of the initial random actions
	        		initnialRandomActions--;
	        		state.updatePosition((DynamicPercept) percept);
	    			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
	    			state.agent_last_action=state.ACTION_SUCK;
	    	    	return LIUVacuumEnvironment.ACTION_SUCK;
	        	}

    			updateKnownState(p);
    			
    		    Boolean dirt = (Boolean)p.getAttribute("dirt");
    		    Boolean home = (Boolean)p.getAttribute("home");

    		    System.out.println("----");
    		    System.out.println("percept: " + p);
    		    System.out.format("x: %d, y: %d, dir: %d\n", state.agent_x_position, state.agent_y_position, state.agent_direction);
    	    	
    		    state.printWorldDebug();
    	    	
    		    // OUR AGENT AI BELLOW
    		    
    			if (dirt) {
    				return doAction(LIUVacuumEnvironment.ACTION_SUCK);
    			} else {
    				// Breadth First Search to find next action that will lead us towards the [target].
    				Action action = bfs(state.agent_x_position, state.agent_y_position, state.agent_direction, target);
    				
    				// When all reachable UNKNOWN cells have been found bfs() will return NO_OP and
    				// we change our target to HOME (4).
    				if (target != 4 && action == NoOpAction.NO_OP) {
    					target = 4; // HOME
    					action = LIUVacuumEnvironment.ACTION_MOVE_FORWARD; // why not?
    				}
    				
    				// When HOME has been reached we want to stop.
    				if (target == 4 && home) {
    					action = NoOpAction.NO_OP;
    				}
    				
    				return doAction(action);
    			}
    		}
    		
    	});
    	
    	
    	/*
    	 * Simple state machine for task 1, with no obstacles.
    	 */
    	
    	/*super(new MyAgentProgram() {
    		
    		int state = 0;
    		
    		public Action execute(Percept percept) {

    		    DynamicPercept p = (DynamicPercept) percept;
    		    Boolean bump = (Boolean)p.getAttribute("bump");
    		    Boolean dirt = (Boolean)p.getAttribute("dirt");
    		    Boolean home = (Boolean)p.getAttribute("home");
    		    
    		    System.out.println("percept: " + p);
    		    
    		    if (dirt.booleanValue())
    		    	return LIUVacuumEnvironment.ACTION_SUCK;
    		    else {
    		    	if (bump) state++;
    		    	
    		    	System.out.println(state);
    		    	
    		    	if (state == 0) {
        		    	System.out.println(state);
    		    		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
    		    	} else if (state == 1) {
    		    		state++;
    		    		return LIUVacuumEnvironment.ACTION_TURN_LEFT;
    		    	} else if (state == 2) {
    		    		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
    		    	} else if (state == 3) {
    		    		state++;
    		    		return LIUVacuumEnvironment.ACTION_TURN_LEFT;
    		    	} else if (state == 4) {
    		    		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
    		    	} else if (state == 5) {
    		    		state++;
    		    		return LIUVacuumEnvironment.ACTION_TURN_LEFT;
    		    	} else if (state == 6) {
    		    		state++;
    		    		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
    		    	} else if (state == 7) {
    		    		state++;
    		    		return LIUVacuumEnvironment.ACTION_TURN_LEFT;
    		    	} else if (state == 8) {
    		    		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
    		    	} else if (state == 9) {
    		    		state++;
    		    		return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
    		    	} else if (state == 10) {
    		    		state++;
    		    		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
    		    	} else if (state == 11) {
    		    		state = 4;
    		    		return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
    		    	} else if (state == 12) {
    		    		if (home) state = 14;
    		    		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
    		    	} else if (state == 13) {
    		    		state = 12;
    		    		return LIUVacuumEnvironment.ACTION_TURN_LEFT;
    		    	}
    		    	
    		    	
    		    }
    		    
    		    return NoOpAction.NO_OP;
    		    
    		}
    	});*/
	}
}
