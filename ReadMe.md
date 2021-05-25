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