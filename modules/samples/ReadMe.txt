Samples -  WSO2 Complex Event Processor
------------------------------------------------

This sample provides a simple scenario to check the event flow from the publisher to the subscriber through the CEP.

There are basic samples given to try how CEP 4.0.0 works with different event adaptors and mappings..

To run the samples...

1) run the command ./wso2cep-samples.sh -sn {sample_id} from <CEP_HOME>/bin
    eg:- ./wso2cep-samples.sh -sn 0001
    This command will startup the server with the sample configuration files.
    The samples can be found in the <CEP_HOME>/samples/artifacts

2) Here with the CEP 4.0.0 alpha-2 pack, there are 7 samples given...


Basic samples are numbered from 0001 to 0099. These samples explain how to configure different types of input/output adaptors and mappings.

Samples with processing queries are numbered from 0100 onwards.

Each sample artifact will have its own ReadMe.txt in samples/artifacts/<sample-no> folder. The readme will contain the event adaptors,
mappings in the sample and will also specify the consumer and producer clients needed for the sample.

Consumer clients are in samples/consumers/ and producer clients are in samples/producers/

Each client can be run through the ant client and any additional options will be printed to the console when running ant.

Here you can traverse through the UI and check the user-friendliness of the UI and check for the new features which
available in CEP 4.0.0















