# Multithreaded Moderator

### Moderator 
A serial program to search through a list of files and scan them for keywords.

### ThreadedModerator 
My first attempt at a concurrent solution to the problem where I created one 
thread per file, which would not scale well if enough files were tossed at the program. There would've
been context-switching across multiple cores and would create a lot over overhead. Runtimes are about 2/3
of the serial solution with 10 testing files of size 10 million lines.

### ThreadedSearcher
My second attempt at a concurrent solution where I only created 8 threads
(one per core on good CPU's, two per core on most) to read in all the files. This takes out some overhead
which would have been created if thousands of files needed to be searched. Runtimes for small testing sets
are comparable to the runtimes of the first concurrency attempt, but this will scale better.

### FileGenerator
A program to generate txt files with many words. I added a chance for a keyword
to be inserted into the files as they are generated so that they appear throughout the files,
putting the threads to work.
