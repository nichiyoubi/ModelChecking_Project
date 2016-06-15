/**
 * 
 */
package line_tracer;

import lejos.hardware.lcd.LCD;
import lejos.hardware.port.*;
import lejos.utility.Delay;
import lejos.hardware.ev3.*;
import lejos.hardware.*;

/**
 * @author usamimasanori
 *
 */
public class LineTracer {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LightSensorImpl light =  new LightSensorImpl(SensorPort.S2);
		WheelImpl rightWheel = new WheelImpl(MotorPort.B);
		WheelImpl leftWheel = new WheelImpl(MotorPort.C);
		DirectionControllerImpl direction = new DirectionControllerImpl(rightWheel, leftWheel);
		ControllerOnOff controller = new ControllerOnOff(light, direction);

		/// ネットワーク・リモート制御スレッドの起動
		RemoteController remoteController = new RemoteController(direction);
		Thread threadController = new Thread(remoteController);
		threadController.start();
		
		///　ネットワーク・データ送信スレッドの起動
		RemoteDataProvider dataProvider = new RemoteDataProvider(light, rightWheel, leftWheel);
		Thread threadProvider = new Thread(dataProvider);
		threadProvider.start();

		/// ライントレーサの起動
		trace(light, controller, direction);
	}
	
	public static void trace(LightSensor light, Controller controller, DirectionController direction) {
		Key enter   = ((EV3)BrickFinder.getLocal()).getKey("Enter");
		
		light.setThreashold(0.15F);
		
		LCD.drawString("Push Start.", 0, 2);
		while(enter.isUp()) {
			Delay.msDelay(100);
		}
		LCD.drawString("           ", 0, 2);
		
		for(int i = 0; i < 1200; i++) {
			Delay.msDelay(100);
			controller.execute();
			Float val = light.getValue();
			LCD.drawString("light=" + String.valueOf(val), 0, 3);
			if (enter.isDown()) {
				direction.stop();
				System.exit(0);
			}
		}
	}
}
