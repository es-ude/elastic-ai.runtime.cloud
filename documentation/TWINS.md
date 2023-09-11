# OVERVIEW
## Inheritance
[![](https://mermaid.ink/img/pako:eNqNkjFPwzAQhf9K5LkdYYiiLi0DEl0IKhLycrKvYBHfRbZThEr-O06cNKg0ap0hvve-d7GjOwrFGkUuVAXebwy8O7CSsrjWbG1DRkEwTA-kazYUsuJnucye0XLAi8DV7BMrqGaiKTxPpA4bPBiFL19m4F-d8eGRAro9KExMr03IFEk20u5ucsfqX3QufLj_6187b4mua-ATPVbJW5e7Qbjk9kc5-bd-b8tkArvb4GjV7E3A8UbnT9ekH46z_3xMVreK4qSuVkluJYmFsOgsGB3Hq6elCB9oUYo8bjW4TykktZGDJnD5TUrkwTW4EE2tIeAwjaNYA70xx3IPlY816u6W22F8u1f7CwL2-LQ?type=png)](https://mermaid.live/edit#pako:eNqNkjFPwzAQhf9K5LkdYYiiLi0DEl0IKhLycrKvYBHfRbZThEr-O06cNKg0ap0hvve-d7GjOwrFGkUuVAXebwy8O7CSsrjWbG1DRkEwTA-kazYUsuJnucye0XLAi8DV7BMrqGaiKTxPpA4bPBiFL19m4F-d8eGRAro9KExMr03IFEk20u5ucsfqX3QufLj_6187b4mua-ATPVbJW5e7Qbjk9kc5-bd-b8tkArvb4GjV7E3A8UbnT9ekH46z_3xMVreK4qSuVkluJYmFsOgsGB3Hq6elCB9oUYo8bjW4TykktZGDJnD5TUrkwTW4EE2tIeAwjaNYA70xx3IPlY816u6W22F8u1f7CwL2-LQ)

# Glossary

## CommunicationEndpoints
It is the Base for all Endpoints. All CEs must have a unique identifier.

## Remote CE
Extends the CE Class. It is used to communicate with other CEs.

## Local CE
Extends the CE Class. It can be used to implement communication participants, e.g. Device Twins or Services.

## Extensions of Local CE

### Device Twin
Implements a Twin for an actual device.

### Services
Services are the interface external users would use. Inside a service, any given functionality can be implemented e.g. a service called 'WristService' could implement necessary functionalities for a that specific use case, like storing g-values.

### Monitor
The monitor has a wildcard to the status of all device twins, meaning the monitor is subscribed to the status of every device. Therefore, it is used to check which devices are available for communication.
Furthermore, it can implement device specific functionalities e.g. flashing bit files.

### CompositeTwin
Composite Twins are made up of multiple twins and combine them into one CE e.g. multiple devices in a room could be part of a composite twin 'room', that would contain all devices inside that room.



---


# Communication Paths
[![](https://mermaid.ink/img/pako:eNptTzsPgkAM_iukM4wuxDjhpouIJuaWhqtykeuRcvgI4b975ysOdur3TDtC7TRBDifBrklWG8VJGOLdrKCLqWmeZYu9mN5vr4Zf4hdGbe3YeCd_lCcoSWKL-o2-uehZ3jwJY1v1FCogBUti0ehw0BgTCnxDlhTkYdUoZwWKp-DDwbvyzjXkXgZKYeg0eioMhj_sh-yQD84FeMS2p-kBhltQSg?type=png)](https://mermaid.live/edit#pako:eNptTzsPgkAM_iukM4wuxDjhpouIJuaWhqtykeuRcvgI4b975ysOdur3TDtC7TRBDifBrklWG8VJGOLdrKCLqWmeZYu9mN5vr4Zf4hdGbe3YeCd_lCcoSWKL-o2-uehZ3jwJY1v1FCogBUti0ehw0BgTCnxDlhTkYdUoZwWKp-DDwbvyzjXkXgZKYeg0eioMhj_sh-yQD84FeMS2p-kBhltQSg)

