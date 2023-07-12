# How To Create Your Own Twin

When creating a Twin, it can be decided how much abstraction is needed.
From this abstraction, a Twin can be created by inheriting from the chosen base implementation.

## Twin

It is the Base for all Twins.

## Twin Stub

Extends the Twin Class.
Implements all functions needed to communicate to a Device which implements the runtime protocol.

## Java Twin

Extends the Twin Class. Implements all runtime protocol functions needed to be a Twin in a runtime environment.

## Device Twin

Extends the Java Twin Class.
Implements all functionality to be Twin for a Device.
It includes a Twin stub for communication with the Device.

## Executable

It can be used to make a Twin Class executable.

## Example

The example [minimalExecutableDeviceTwin.java](../runtime/src/main/java/org/ude/es/twinImplementations/minimalExecutableDeviceTwin.java)
shows how a Device Twin can be created and be made executable.
