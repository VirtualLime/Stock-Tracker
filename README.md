# CS2910-A5-Stocks
Stock Portfolio Tracker

Due Date: November 29, 2019 11:59pm

For this assignment you will create a Server that allows clients to connect and keep track of their favorite stock prices.

## Server

Your server should be multithreaded and handle the below commands, where all commands are followed by an exclaimation point as shown below:

1. USER username!  
*allow a user to log in, where username is their login name*
response: ok! / error! 

2. PORTFOLIO! 
*view all of the stocks and their current prices that this user wants to track*
response: 
for each stock the user wishes to track a line as follows:
ticker price
the last line is terminated with a !. Note that ticker is the stock ticker such as aapl for Apple company.

3. TRACK ticker! 
*add a particular ticker to the user's list of stocks they want to track
response: 
ok! / error! 

error in the case the ticker does not exist

4. FORGET ticker!
response: 
ok!

even if the ticker is not currently being tracked or is not a valid ticker just respond ok

5. All other commands should receive a response of:
error! 

To retrieve current stock ticker prices you should use the free API provided by finacialmodelingprep.com.
You may access a stock price using the following base url:
"https://financialmodelingprep.com/api/v3/stock/real-time-price/";
but then appending the string for the stock ticker, such as for Apple Company with ticker aapl:
"https://financialmodelingprep.com/api/v3/stock/real-time-price/aapl";

This website will return a JSON string containing the ticker symbol and price. You are provided a bit of sample code for parsing the price out of this construct in the provided IntelliJ project.

We will cover HttpClients to make this web connection in coming classes, although you may also use sockets as we did previously to obtain website data. As we will see the HttpClient is a nicer way to do this.

## Client:
You should provide a Simple Client that works like our SimpleBankClient from class. Allow the user to interact with your server by typing commands at the console. 

Your client should connect to your server and allow the user to send any of the above commands and then display the results to the console. 

For example here is an example interaction:

Client: USER andrew!

Server: ok!

Client: TRACK aapl! 

Server: ok!

Client: TRACK amzn!

Client: PORTFOLIO!

Server: amzn 1771.75\n

aapl 262.11\n

!

Client: FORGET aapl!

Server: ok!

Client: PORTFOLIO!

Server: amzn 1771.75\n

!

You may print the text out in more readable formats or prompt the user for input in any way you deem intuitive. 

Using an end of transmission ! character should make sending and receiving messages much easier. Just add it to anything on the out stream and then use a delimiter !. So the user wouldn't actually see or type the delimiter. It will just save you from having the server or client waiting for input, it knows ! is the end of a transmission.

## Persistence

Your program must implement data persistence. You must choose either a binary file format for data persistence or a database (to be covered in coming classes). This means that if you shut down your server and then restart it, a user may log in and still retreive their portfolio that they have set up on a previous session.

## Notes:

You should ensure your program is thread safe since two clients may access the same portfolio at the same time.

## grades

2 points code readability

4 points code design

4 points functionality






