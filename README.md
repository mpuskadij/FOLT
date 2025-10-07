# FOLT
Web app developed using Java to simulate a company, its partners and clients. It uses virtual threads on multiple servers (each server is executed in its own Docker container) with UI being done in JSP and Jakarta Faces

## Architecture
<img width="962" height="881" alt="arhitektura" src="https://github.com/user-attachments/assets/65a003ee-684c-465b-a752-bb86619173ad" />

### PolužiteljTvrtka
This class starts 3 different servers on different ports after at least 1 kitchen and/or 1 drinks menu have been loaded. All kitchens share the same drinks menu, while each kitchen has its own food menu. Kitchens have to be in a separate json/xml/binary/test file with file names have a prefix "kuhinja_x" where x is  number ranging from 1-9. Drinks menu is loaded by file name whose name is dictated by the configuration file. After that, this class loads all registered partners that have to be in another file whose name is specified in configuration for the partner.  To start these servers, virtual threads are used. Each server for every incoming request creates a new virtual thread that handles the request. 

 ### Poslužitelj za kraj rada
 This server shuts down itself and 2 other servers. 
 Commands:
| Syntax | Description |
| ----------- | ----------- |
| KRAJ xxx | If xxx is the code specified by the configuration file, the command shuts down all 3 servers. |
| STATUS xxx [1,2] | Returns the status of the specified server (1 - registration, 2 - partners) |
| PAUZA xxx [1,2] | Puts a certain server in pause (not handling requests) |
| START xxx [1,2] | Starts a specified server (if paused) |
| SPAVA xxx n | Sleeps for n miliseconds |
