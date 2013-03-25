package com.objectgraph.core;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.junit.Test;

import com.objectgraph.jobsystem.JobNode;
import com.objectgraph.jobsystem.PausableAsyncService;
import com.objectgraph.jobsystem.PausableAsyncTask;
import com.objectgraph.pluginsystem.PluginConfiguration;
import com.objectgraph.pluginsystem.PluginManager;

public class JobNodeTest extends Application {
	
	public static class ServiceExample extends JobNode {
		
		@Job
		public void testService() {
			startJob();
			for(int i = 0; i < 10; i++) {
				startCycle(i, 10);
				JobNode.sleep(10);
				JobNode.sleep(10);
//				System.out.println(getProgress());
				JobNode.sleep(10);
				anotherService();
				JobNode.sleep(10);
//				System.out.println(getProgress());
				JobNode.sleep(10);
				System.out.println("Crunching " + (i+1));
				JobNode.sleep(10);
				endCycle();
			}
			JobNode.sleep(10);
			endJob();
		}

		@Job
		public void anotherService() {
			startJob();
			JobNode.sleep(10);
			JobNode.sleep(10);
//			System.out.println(getProgress());
			JobNode.sleep(10);
			System.out.println("Another");
			JobNode.sleep(10);
//			System.out.println(getProgress());
			JobNode.sleep(10);
			JobNode.sleep(10);
			endJob();
		}
		
	}
	
	private static class Wrapper {
		public boolean content = false;
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
	/*
        final ProgressBar bar = new ProgressBar();
		
		PluginConfiguration conf = new PluginConfiguration("main");
		conf.getLibraries().add(new File("example-plugin_fat.jar"));
		PluginManager.setConfiguration(conf);
		
		JobNode example = PluginManager.getImplementations(JobNode.class).get(0);
		System.out.println(example.getClass());
		
//		ServiceExample example = new ServiceExample();
		final PausableAsyncService<Void> service = new PausableAsyncService<>(example, "testJob");
		bar.progressProperty().bind(service.progressProperty());
		HBox box = new HBox(5);
		
		Button pause = new Button("Pause");
		pause.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (service.isPaused())
					service.resume();
				else
					service.pause();
			}
		});
		Button cancel = new Button("Cancel");
		cancel.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				service.cancel();
			}
		});
		final Wrapper problem = new Wrapper();
		Button restart = new Button("Restart");
		restart.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				problem.content = false;
				service.restart();
			}
		});
		final Label state = new Label();
		service.stateProperty().addListener(new ChangeListener<Worker.State>() {
			@Override
			public void changed(ObservableValue<? extends State> arg0,
					State arg1, State arg2) {
				state.setText(arg2.name());
			}
			
		});
		
		box.getChildren().addAll(bar, pause, cancel, restart, state);
		primaryStage.setScene(new Scene(box));
//		primaryStage.show();
		
		service.progressProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (oldValue.doubleValue() > newValue.doubleValue()) {
					if (problem.content == false) {
						problem.content = true;
					} else {
						System.out.println("PROBLEM " + oldValue + " " + newValue);
						System.exit(1);
					}
				}
			}
		});
		
		service.setOnFailed(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				service.getException().printStackTrace();
			}
		});

//		service.start();
		
		PausableAsyncTask<Void> s = new PausableAsyncTask<>(example, "testJob");
		ExecutorService ex = Executors.newFixedThreadPool(10);
		Future<?> f = ex.submit(s);
		f.get();
		System.out.println("Fine");
		ex.shutdown();
        */
        primaryStage.show();
	}

	@Test
	public void test() throws InterruptedException {
		launch();
	}

}
