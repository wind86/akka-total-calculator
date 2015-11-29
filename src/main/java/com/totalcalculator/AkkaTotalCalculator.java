package com.totalcalculator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.totalcalculator.util.FileGenerator;
import com.totalcalculator.util.ResultCache;
import com.totalcalculator.util.ResultWriter;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.routing.RoundRobinRouter;
import akka.util.Duration;

public class AkkaTotalCalculator implements TotalCalculator {

	public static void main(String[] args) throws IOException {
		final AkkaTotalCalculator totalCalculator = new AkkaTotalCalculator();
		totalCalculator.calculate("demo.txt", "akka-result.txt");
	}

	@Override
	public void calculate(final String inputFileName, final String outputFileName) throws IOException {
		final File file = FileGenerator.generate(inputFileName);

		final ActorSystem system = ActorSystem.create("TotalCalculator");
		@SuppressWarnings("serial")
		final ActorRef resultListener = system.actorOf(new Props(new UntypedActorFactory() {
			public Actor create() {
				return new ResultListener(outputFileName);
			}
		}), "resultListener");

		@SuppressWarnings("serial")
		final ActorRef manager = system.actorOf(new Props(new UntypedActorFactory() {
			public Actor create() {
				return new Manager(file, resultListener);
			}
		}), "manager");

		manager.tell(new Calculate());
	}

	public static class ResultListener extends UntypedActor {

		private final String outputFileName;

		public ResultListener(final String outputFileName) {
			this.outputFileName = outputFileName;
		}

		@Override
		public void onReceive(final Object message) throws Exception {
			if (message instanceof Duration) {
				final Duration duration = (Duration) message;
				System.out.println(String.format("\nCalculation time: \t%s", duration));

				ResultWriter.writeResults(outputFileName, ResultCache.INSTANCE.getAll());

				getContext().system().shutdown();
			} else {
				unhandled(message);
			}
		}
	}

	public static class Manager extends UntypedActor {		

		private static final String LINE_SEPARATOR = System.lineSeparator();
		
		private final long start = System.currentTimeMillis();

		private final ActorRef listener;
		
		private final ActorRef workerRouter;
		private final File file;

		private int readLinesCount;
		private int processedCounter;

		public Manager(final File file, final ActorRef listener) {
			this.listener = listener;
			this.file = file;
			this.workerRouter = getContext()
					.actorOf(new Props(Worker.class).withRouter(new RoundRobinRouter(5)), "workerRouter");
		}

		@Override
		public void onReceive(final Object message) throws Exception {
			if (message instanceof Calculate) {
				processFile();
			} else if (message instanceof Result) {
				processedCounter++;

				if (processedCounter == readLinesCount) {
					final Duration duration = Duration.create(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
					listener.tell(duration, getSelf());
					getContext().stop(getSelf());
				}
			} else {
				unhandled(message);
			}
		}

		private void processFile() throws FileNotFoundException, IOException, URISyntaxException {
			final long startTime = System.currentTimeMillis();
			
			try (final SeekableByteChannel ch = Files.newByteChannel(Paths.get(file.toURI()))) {
			    final ByteBuffer bb = ByteBuffer.allocateDirect(8 * 1024);
			    final StringBuilder lineBuilder = new StringBuilder();
			    
			    while (ch.read(bb) > 0) {
					bb.flip();
					
					for (int i = 0; i < bb.limit(); i++) {							
						lineBuilder.append((char) bb.get());
					}

					bb.clear();			        
			    }

			    final String[] lines = StringUtils.split(lineBuilder.toString(), LINE_SEPARATOR);
			    for (final String line : lines) {
					workerRouter.tell(new Work(line), getSelf());
			    }			    
			    readLinesCount = lines.length;
			}
			
			System.out.println("proccessing file : " + (System.currentTimeMillis() - startTime));
		}
	}
	

	public static class Worker extends UntypedActor {

		private static final Result RESULT = new Result();
		
		@Override
		public void onReceive(final Object message) throws Exception {
			if (message instanceof Work) {
				final Work work = (Work) message;
								
				final String data[] = work.getData().split(SEPARATOR);

				final Integer id = Integer.valueOf(data[0]);
				final Integer value = Integer.valueOf(data[1]);

				calculate(id, value);

				getSender().tell(RESULT, getSelf());
			} else {
				unhandled(message);
			}
		}

		private void calculate(final Integer key, Integer value) {
			final Integer currentValue = ResultCache.INSTANCE.get(key);
			if (currentValue != null) {
//				if (key == 1) {
//					System.out.println("value: " + value + " + current value : " + currentValue);
//				}
				value += currentValue;
			}

//			if (key == 1) {
//				System.out.println("value: " + value);
//			}
			
			ResultCache.INSTANCE.put(key, value);
		}
	}

	// Messages
	static class Calculate {}

	static class Result {}
	
	static class Work {
		private final String data;

		public Work(String data) {
			this.data = data;
		}

		public String getData() {
			return data;
		}
	}
}
