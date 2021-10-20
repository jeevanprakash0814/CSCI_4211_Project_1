# Project 1 CSCI 4211
## Jeevan Prakash,Zhang CSCI4211,20/10/2021
## Java,DNSServer.java,DNSServer.class
## Compilation Section
1. Run ``javac DNSServer.java``
2. Run ``javac DNSClient.java``
## Execution Section
1. Run ``java DNSServer`` in one terminal window
2. Run ``java DNSClient`` in another terminal window
3. Starting sending queries through client
## Description Section
This program does socket server communication over TCP with clients. It is meant to field DNS requests for domains and return them as responses back to the individual asking. Also, it is multi-threaded which means that multiple requests can technically occur at once. It is a quite simple socket structure. It also keeps a cache of all responses that have already been tried which make loading up IPs faster.