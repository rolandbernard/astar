package net.tfobz.astern;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.model.Statement;

import net.tfobz.astern.AStern.Node;

@RunWith(Enclosed.class)
public class ASternTest {

	@RunWith(Parameterized.class)
	public static class TestIllegalArgument{
		private String fileString = null;
		
		public TestIllegalArgument(String fileString) {
			this.fileString = fileString;
		}
		
		@Parameters
		public static Collection<String> testCases() {
			return Arrays.asList(
					"a 0: 0.9; 0.9",
					"n 0; -0.1; 0.2",
					"n 0; 0.1; -0.2",
					"n 0: -0.1; -0,1",
					"n 0: 0.0; 0.0\nn 1: 1.0; 1.0\nc 0 <=> 2",
					"hallo ich bin kein graph",
					"n 12:hans:sepp"
			);
		}
		
		@Test (expected=IllegalArgumentException.class)
		public void test() throws IllegalArgumentException, IOException {
			new AStern(new BufferedReader(new StringReader(this.fileString)));
		}
	}
	
	public static class TestTimeout {
		private static final int MIN_TIMEOUT = 100;

		@SuppressWarnings("deprecation")
		@Rule
		public Timeout timeout = new Timeout(MIN_TIMEOUT) {
			public Statement apply(Statement base, Description description) {
				return new FailOnTimeout(base, MIN_TIMEOUT) {
					@Override
					public void evaluate() throws Throwable {
						try {
							super.evaluate();
							throw new TimeoutException();
						} catch (Exception e) {}
					}
				};
        	}
    	};	
    	
    	@Test(expected = TimeoutException.class)
    	public void givesTimeout() throws InterruptedException, FileNotFoundException, IllegalArgumentException, IOException {
    		AStern as = new AStern("test.txt");
    		as.setWaitTime(1000);
    		as.getPathFromTo(as.getNode("start"), as.getNode("end"));
    	}
	}

	@RunWith(Parameterized.class)
	public static class TestIllegalArgumentExecution {	
		private AStern as = null;
		
		public TestIllegalArgumentExecution(String fileString) throws IllegalArgumentException, IOException {
			this.as = new AStern(new BufferedReader(new StringReader(fileString)));
		}
		
		@Parameters
		public static Collection<String> testCases() {
			return Arrays.asList(
					"",
					"n 2: 0;0\nn 3: 1;1\nc 2 == 1",
					"n 0: 0;0\nn 1: 1;1",
					"n 0: 0;0\nn 2: 1;1\nc 0 == 2",
					"n 2: 0;0\nn 1: 1;1\nc 1 == 0",
					"n 0: 0;0\nn : 0;1\nn 2: 1;1\nn 1: 1;2\nc 1 == 2\nc! 0|2 == :0;1\n"
			);
		}
		
		@Test (expected=IllegalArgumentException.class)
		public void test() throws IllegalArgumentException, IOException {
			as.getPathFromTo(as.getNode("0"), as.getNode("1"));
		}
	}
	
	public static class TestNoError {
		@Test
		public void testAddNode() {
			AStern as = new AStern();
			
			Node a = new Node(null, 0, 0);
			Node b = new Node("", 1, 1);
			Node c = new Node("name", 2, 2);
			Node d = new Node(".*.", -1, -1);
			
			as.addNode(a);
			as.addNode(b);
			as.addNode(c);
			as.addNode(d);
			
			Collection<Node> asNodes = as.getNodes();
			assertEquals(4, asNodes.size());
			assertTrue(asNodes.contains(a));
			assertTrue(asNodes.contains(b));
			assertTrue(asNodes.contains(c));
			assertTrue(asNodes.contains(d));
		}
		
		@Test
		public void testPathCost() {
			AStern as = new AStern();
			
			Node a = new Node(null, 0, 0);
			Node b = new Node(null, 1, 0);
			Node c = new Node(null, 1, 1);
			Node d = new Node(null, 0, 1);
			
			as.addNode(a);
			as.addNode(b);
			as.addNode(c);
			as.addNode(d);
			
			as.setConnection(a, b, 1);
			as.setConnection(b, c, 1);
			as.setConnection(c, d, 1);
			as.setConnection(d, a, 1);
			as.setConnection(a, d, 1);
			
			List<Node> path = new ArrayList<>();
			path.add(a);
			path.add(b);
			path.add(c);
			path.add(d);
			path.add(a);
			path.add(d);
			
			assertEquals(5, as.getPathCost(path));
		}
		
		@Test
		public void testShortestPath() {
			AStern as = new AStern();
			
			Node a = new Node(null, 0, 0);
			Node b = new Node(null, 1, 0);
			Node c = new Node(null, 1, 1);
			Node d = new Node(null, 0, 1);
			
			as.addNode(a);
			as.addNode(b);
			as.addNode(c);
			as.addNode(d);
			
			as.setConnection(a, b, 1);
			as.setConnection(b, c, 1);
			as.setConnection(c, d, 1);
			as.setConnection(d, a, 1);
			as.setConnection(a, d, 1);
			
			List<Node> path = as.getPathFromTo(a, d);
			assertEquals(Arrays.asList(a, d), path);
		}
		
		@Test
		public void testGetNode() {
			AStern as = new AStern();
			
			Node a = new Node("1", 0, 0);
			Node b = new Node("2", 1, 0);
			Node c = new Node("3", 1, 1);
			Node d = new Node("4", 0, 1);
			
			as.addNode(a);
			as.addNode(b);
			as.addNode(c);
			as.addNode(d);
			
			assertEquals(a, as.getNode("1"));
			assertEquals(b, as.getNode("2"));
			assertEquals(c, as.getNode("3"));
			assertEquals(d, as.getNode("4"));
		}
	
		@Test
		public void testLoad() throws IllegalArgumentException, IOException {
			AStern as = new AStern(new BufferedReader(new StringReader("n start: 0;0\nn end: 1;0\nn : 1;1\nc :1;1 <==> end\nc start =4=> :1;1")));
			
			assertEquals(5, as.getPathCost(as.getPathFromTo(as.getNode("start"), as.getNode("end"))));
		}
		
		@Test
		public void testLoadFile() throws FileNotFoundException, IllegalArgumentException, IOException {
			AStern as = new AStern("graph1.dat");
			assertEquals(1.8, as.getPathCost(as.getPathFromTo(as.getNode("start"), as.getNode("end"))), 0.0001);
		}
	}
	
}
