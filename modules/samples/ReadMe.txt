Samples -  WSO2 Complex Event Processor
------------------------------------------------

This directory provides simple scenarios to illustrate CEP functionality with different configurations.

To run the samples...

From Linux :
    run the command ./wso2cep-samples.sh -sn {sample_id} from <CEP_HOME>/bin
    eg:- ./wso2cep-samples.sh -sn 0001

From Windows :
    run the command wso2cep-samples.bat -sn {sample_id} from <CEP_HOME>/bin
    eg:- wso2cep-samples.bat -sn 0001

This command will startup the server with the sample configuration files.
The samples can be found in the <CEP_HOME>/samples/cep/artifacts


Basic samples are numbered from 0001 to 0099. These samples explain how to configure different types of event receivers & publishers and their mappings.
Samples with processing queries are numbered from 0100 onwards.

Each sample artifact will have its own ReadMe.txt in samples/cep/artifacts/<sample-no> folder.
The readme will contain the Producer & Consumer clients needed for the sample to run.

Consumer clients are in samples/cep/consumers/ and Producer clients are in samples/cep/producers/

Each client can be run through the provided ant script and additional options it supports will be printed to the console when running ant.

For more infomaiton of running samples please refer https://docs.wso2.com/display/CEP420/Samples+Guide















