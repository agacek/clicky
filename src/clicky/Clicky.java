package clicky;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Clicky {
	private static Display display;

	private static Text x;
	private static Text y;
	private static Text delay;
	private static Text randomness;

	private static Label countdown;

	private static Button grab;
	private static Button stop;
	private static Button start;

	private static Thread thread;
	private static Random random = new Random();

	public static void main(String[] args) {
		display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Clicky Clicky Clicky");

		GridLayout layout = new GridLayout();
		GridData gridData;
		layout.numColumns = 4;
		layout.makeColumnsEqualWidth = true;
		shell.setLayout(layout);

		new Label(shell, SWT.None).setText("X");
		new Label(shell, SWT.None).setText("Y");
		new Label(shell, SWT.None).setText("Min Delay");
		new Label(shell, SWT.None).setText("Randomness");

		x = new Text(shell, SWT.BORDER);
		x.setText("20");
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		x.setLayoutData(gridData);

		y = new Text(shell, SWT.BORDER);
		y.setText("20");
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		y.setLayoutData(gridData);

		delay = new Text(shell, SWT.BORDER);
		delay.setText("5");
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		delay.setLayoutData(gridData);

		randomness = new Text(shell, SWT.BORDER);
		randomness.setText("3");
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		randomness.setLayoutData(gridData);

		countdown = new Label(shell, SWT.None);
		countdown.setText("Countdown: ");
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = SWT.FILL;
		countdown.setLayoutData(gridData);

		grab = new Button(shell, SWT.PUSH);
		grab.setText("Grab Pointer");
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		grab.setLayoutData(gridData);
		grab.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				grabPointer();
			}
		});

		stop = new Button(shell, SWT.PUSH);
		stop.setText("Stop");
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		stop.setLayoutData(gridData);
		stop.setEnabled(false);
		stop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stopThread();
			}
		});

		start = new Button(shell, SWT.PUSH);
		start.setText("Start");
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		start.setLayoutData(gridData);
		start.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startThread();
			}
		});

		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event e) {
				if (thread != null) {
					thread.interrupt();
				}
			}
		});

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private static void grabPointer() {
		display.timerExec(3000, new Runnable() {
			@Override
			public void run() {
				Point loc = display.getCursorLocation();
				x.setText(Integer.toString(loc.x));
				y.setText(Integer.toString(loc.y));
			}
		});
	}

	private static void startThread() {
		start.setEnabled(false);
		thread = new Thread() {
			@Override
			public void run() {
				try {
					runThread();
				} catch (InterruptedException e) {
					// User clicked stop
				}
			}
		};
		thread.start();
		stop.setEnabled(true);
	}

	private static void runThread() throws InterruptedException {
		while (true) {
			int wait = getWait();
			for (int i = wait; i >= 1; i--) {
				if (display.isDisposed()) {
					return;
				}
				setCountdown(i);
				Thread.sleep(1000);
			}
			doClick();
		}
	}

	private static int getWait() {
		final int[] wait = { 0 };
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				int fixed = Integer.parseInt(delay.getText());
				int variable = Integer.parseInt(randomness.getText());
				wait[0] = fixed + random.nextInt(1 + variable);
			}
		});
		if (wait[0] > 0) {
			return wait[0];
		} else {
			return 1;
		}
	}

	private static void setCountdown(final int i) {
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				countdown.setText("Countdown: " + i);
			}
		});
	}

	private static void stopThread() {
		stop.setEnabled(false);
		thread.interrupt();
		start.setEnabled(true);
	}

	private static void doClick() {
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					Robot robot = new Robot();
					robot.mouseMove(Integer.parseInt(x.getText()), Integer.parseInt(y.getText()));
					robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				} catch (AWTException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
