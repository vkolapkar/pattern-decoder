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
