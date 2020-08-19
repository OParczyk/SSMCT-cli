# Intended functions and flow of SSMCT-cli

This is - as is everything else here - heavily WIP and may change without notice. Once I figure the first version to be stable this message will disappear, developement will be moved to another branch and significant changes to the CLI won't break older scripts. If the latter cannot be guaranteed a warning will be placed here in advance.


## Guided interactive mode

This mode will prompt you for parameters as the need for them arises. If sufficient information is already provided via command line arguments corresponding sections will be skipped.
This mode is most likely to be the first one to be implemented.

It should be possible to go back steps.

First the user shall be promptedd[D to supply the file to be processed. Only if the neccessary flag for explicitly using two files is given the user is prompted again to supply the second one.
Then available segmenters are to be displayed and the user is to be provided with means to select one. Following this the user is then prompted for the special required parameters for the selected segmenter, if there are any.
Then comparators need to be selected and configured in similar fashion.
Lastly the path for an output file is to be selected. For now the only intended formats supported will be gzipped csv or png.

## interactive command line session

It will be possible to add command line arguments in order to set parameters for parts of the interactive mode and only prompt user input, where it's neccessary for the program to determine its task.
Further arguments for modes of strictness shall be implemented for use in scripts where user input is undesirable.
Details will be outlined in unattended mode setting

