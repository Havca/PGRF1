# PGRF1 Project 1

Project 1 for the **PGRF1** course at the Faculty of Informatics and Management, University of Hradec Králové.\
Created by Ondřej Havelka

## Requirements

* Java 17 or newer
* IntelliJ IDEA or another Java IDE

## Running the project

Clone or download the repository, open the project in your IDE, and run the `Main` class.

## Usage

Dragging mouse while pressed creates a line.\
Clicking creates a point in a new polygon and shows preview line.\
To end drawing the polygon click on the starting point of the polygon.\
When switched to edit mode you can drag around the point of polygons, drawing new objects is now turned off.\

### Keybinds:
* `C` = clears the drawing board
* `Z` = step back function
* `E` = switches between edit mode and vertex insert mode

## Project structure

* `Main` - application entry point
* `Window` – creates and configures the main application window
* `Controller` – handles user input and coordinates application flow
* `Canvas` – provides the raster drawing surface and displays the rendered image
* `LineRasterizer` - algorithm that rasterizes lines onto the canvas, currently using Trivial Line Algorithm

## Course

**PGRF1 (Computer Graphics)**  
Faculty of Informatics and Management  
University of Hradec Králové