package com.aps490.drdc.prototype;

import java.util.HashMap;
import java.util.Map;

public class CourseModules{
  static final Map<String, String> map;
  static {
      map = new HashMap<String, String>();
      map.put("Test Table Assembly", "tableAssembly.xml" );
      map.put("Main Landing Gear", "mainLandingGear.xml" );
      map.put("Adjustment of Door Latch Mechanism", "adjustmentOfDoorLatchMechanism.xml" );
      map.put("Bleeding and Functional Test", "bleedingAndFunctionalTest.xml" );
      map.put("Brake Master Cylinder Adjustment", "brakeMasterCylinderAdjustment.xml" );
      map.put("Brake System", "brakeSystem.xml" );
      map.put("Door Link", "doorLink.xml" );
      map.put("Leg Door", "legDoor.xml" );
      map.put("Main Landing Gear Actuator", "mainLandingGearActuator.xml" );
      map.put("Main Landing Gear Door", "mainLandingGearDoor.xml" );
      map.put("Main Landing Gear Uplock", "mainLandingGearUplock.xml" );
      map.put("Main Landing Gear Uplock Actuator", "mainLandingGearUplockActuator.xml" );
      map.put("Main Landing Gear Wheel", "mainLandingGearWheel.xml" );
      map.put("Main Wheel Door Actuator", "mainWheelDoorActuator.xml" );
      map.put("Main Wheel Door Actuator Spigot", "mainWheelDoorActuatorSpigot.xml" );
      map.put("Parking Brake Control Adjustment", "parkingBrakeControlAdjustment.xml" );
      map.put("Side Stay", "sideStay.xml" );
  }
}
