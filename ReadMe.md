#Main Class DecoderMain
#1 Reads constants on startup & maintain in cache from the source folder configured below
E.g. 
	private static String sourceFolderUtil = ".\\src\\com\\src\\util\\";
#2 Based on source directory configured under sourceFolder read all files recursively for the pattern configured and derieves key value
E.g. sourceFolder = ".\\src\\com\\src\\bl";




echo "# pattern-decoder" >> README.md
git init
git add README.md
git commit -m "first commit"
git branch -M main
git remote add origin https://github.com/vkolapkar/pattern-decoder.git
git push -u origin main


Issue while pushing repo 

#1 : Add your sshe public key to github account 
on Git bash run & generate certificate just keep on entering no need to put any details
ssh-keygen -t rsa -C "vinayak.kolapkar@newscapeconsulting.com"  
#2 :
https://stackoverflow.com/questions/7438313/pushing-to-git-returning-error-code-403-fatal-http-request-failed

#Main Class PatternDecoderMain
* Reads constants on startup & maintain in cache from the source folder configured in config.properties file
e.g. sourceFolderUtil=.\\src\\com\\src\\util\\
* Based on source directory configured in config file, read all files for the pattern configured and derives key-value
e.g. sourceFolder = ".\\src\\com\\src\\bl";

##JAVA version
* JDK 8 or above

##Using PatternDecoderMain to detect custom patterns
* Include pattern in config.properties. For searching multiple tokens as part of one pattern, use '|' operator to separate the tokens in config file. The code will treat them as separate tokens and checkForExpectedPattern() function will return corresponding pattern type (1, 2, etc) based on the matching tokens. 
e.g. RuleToErrorCodeMapping.|ErrorCodeConstants.

* If non-occurence of some tokens is required along with the occurence of other tokens, add a '!' at the start of the token in the config file
e.g. RuleToErrorCodeMapping.|!ErrorCodeConstants. Here, occurence of RuleToErrorCodeMapping. is needed as well as non-occurence of ErrorCodeConstants.

* Use escape characters to escape any special characters in the token in config file, since token gets treated as regex in the code.
e.g. @NotNull\\( - '\\' to escape '('

* Add the pattern in patternsMap hashMap in startupTask() fn.
e.g., if adding pattern 10, add this in startupTask() - patternsMap.put("10", prop.getProperty("pattern10"));

* Include case for the pattern in errorCodeNMsg() function. Provide a method to parse the matching line for key-value. 
e.g. case 10: errorCodeMsgArr = parsePattern10(line);
Provide method body for parsePattern10 depending on your custom rules for parsing the matching line.
