# COMMUNICATION_ENDPOINTS

All Communication Endpoints (CE) must posses an unique identifier. A CE must be connected to a broker.

## CommunicationEndpoints

It is the Base for all Endpoints.

## Remote CE

Extends the CE Class. It is used to communicate with other CEs.

## Local CE

Extends the CE Class. It can be used to implement MQTT participants, e.g. Device Twins or Services.

## Device Twin

Extends the Local CE Class. It can be used as a Twin for a Device.
