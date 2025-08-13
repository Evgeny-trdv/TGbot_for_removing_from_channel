# **TGbot_for_removing_from_channel**

**Project:** telegram bot for providing information about a subscription to private channels and with functions for managing users who have purchased a subscription.

**Admin** - the role of managing and verifying subscription payments \
**Users** - the role of getting information about private channels and the ability to purchase subscriptions

## **Requirements**
Java 17\
Telegram\
Maven\
PostrgeSQL\
Liquibase

## **Main features** 
1. Information about access to private telegram channels
2. List of private telegram channels
3. Subscription information and payment method
4. Support and communication with the admin
5. Sending subscription termination notifications
6. Automatic and manual (as an admin) closure of access to private channels 

### **Install**
- Clone this repository to your local PC\
`git clone https://github.com/Evgeny-trdv/TGbot_for_working_with_user.git`
or Download this repository as zip and unzip\
- Go to TGbot_for_working_with_user folder. In terminal , type the following commands\
`mvn clean package`\
After compiled successfully, you will get the fat jar with name\
`java -jar target/TelegramBotSupportApplication.jar`
