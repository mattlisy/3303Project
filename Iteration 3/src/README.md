# Iteration 2

## Authors
- Alizée Drolet - 101193138
- Milo Goodfellow - 101196365
- Matthew Lisy - 101231092
- Janice Tagak - 101172920
- Alain Xu - 101219003

## Overview

This iteration focuses on three subsystems: the elevator, floor, and scheduler.

Elevator Subsystem (Client):
- Requests work from the scheduler
- Sends data across the server

Floor Subsystem (Client):
- Input data is sourced from a .csv file and parsed into a data structure.
- Each line in this data structure is then sent to the scheduler.
- Receives service data from the scheduler.

Scheduler (Server):
- Receives data from elevator and from floor subsystems.
- Assigns work to elevators.
- Transfers data between floors and elevators.

Summary:
- The elevator and floor subsystems serve as clients interacting with the scheduler as the server.
- Input data, originating from a .csv file, is structured and fed into the scheduler.
- The elevator requests and receives work from the scheduler, transmitting data via the server to the floor.

## Files

- Main.java ~ The main class to initiate the simulation and create threads.
- Floor.java ~ Represents a floor in the elevator system. Reads input file and sends data to the scheduler as a data structure. Receives service data from the scheduler.
- ElevatorCar.java ~ Represents an elevator car in the elevator system. Waits for work from the scheduler. Returns service data to the scheduler.
- Scheduler.java ~ Represents the underlying system to schedule elevator cars to floors. Sends data from floors to elevators. Returns service data to floors.
- Structure.java ~ A data structure containing time (hh:mm:ss:ms), floor source, floor button, and floor destination.

## Setup

1. Launch IntelliJ IDEA
2. Open the project by selecting "File">"Open">"yourprojectdir"
3. Build project by selecting "Build">"Build Project"
4. Check to see if the input file is in the same directory as the project
5. Right click on main.class, click "Run Main.class"

## Responsibilities

Alizée Drolet:
- Floor.java
- Structure.java
- README.txt

Milo Goodfellow:
- Scheduler.java
- Class diagram
- README.txt

Matthew Lisy:
- Sequence diagram
- ElevatorCar.java
- README.txt

Janice Tagak:
- Structure.java
- README.txt

Alain Xu:
- State diagram
- README.txt
