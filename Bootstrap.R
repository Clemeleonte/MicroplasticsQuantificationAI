## Bootstrap-script to estimate (analytical) uncertainty involved in microplastic count

# Set the library path
.libPaths("M:/My Documents/R/win-library/3.6")

# Choose the working directory
Work_Dir <- choose.dir()

# Get the subdirectories in the working directory
dirs <- list.dirs(path = Work_Dir, full.names = FALSE, recursive = FALSE)

# Set the output CSV file name and path
CSV_file <- file.path(Work_Dir, "Final_Results.csv")

# Set the data folder path
Datafolder <- "W:/WFSR/Projects"

# Set the number of repetitions and other parameters
Reps <- 10000
NPhotos <- 21
PhotoType <- c(7, 7, 7)
Target <- "WT" #"WT" or "Cnt" to sample weights or counts, respectively

# Loop through each subdirectory
for (dir in dirs) {
  
  # Write the subdirectory name to the CSV file
  write.table(dir, file = CSV_file, append = TRUE, sep = ",", row.names = "File:", col.names = FALSE)
  
  # Set the path to the data folder in the subdirectory
  Datafolder <- file.path(Work_Dir, dir)
  setwd(Datafolder)
  
  # Get the result files in the data folder
  resfiles <- list.files(pattern = "Results.csv")
  
  # Initialize the result vectors
  R.Wt_indiv <- list()
  R.Wt <- vector()
  R.Count <- vector()
  R.Type <- vector()

  # Loop through each result file
  for (f in 1:length(resfiles)) {
    temp <- data.frame(read.csv(resfiles[f]))
    temp <- temp[-1, ]
    R.Wt_indiv[[f]] <- temp$Weight
    R.Wt[f] <- sum(temp$Weight)
    R.Count[f] <- length(which(temp$Area > 0))
    
    temp.type <- unlist(strsplit(resfiles[f], "_"))
    temp.type <- unlist(strsplit(temp.type[length(temp.type) - 2], split = ""))
    R.Type[f] <- temp.type[temp.type %in% c("C", "R", "M")]
  }
  
  # Initialize the bootstrap vector
  BS <- vector()
  
  # Set the target variable for bootstrap resampling
  if (Target == "WT") {
    R.target <- R.Wt
  } else if (Target == "Cnt") {
    R.target <- R.Count
  }
  
  # Generate bootstrap samples and calculate means
  for (R in 1:Reps) {
    if (length(PhotoType) != 3) {
      add.this <- sample(R.target, size = NPhotos, replace = TRUE)
    }
    if (length(PhotoType) == 3) {
      N.per.type <- round(PhotoType / sum(PhotoType) * NPhotos, 0)
      add.this <- c(sample(R.target[R.Type == "C"], size = N.per.type[1], replace = TRUE),
                    sample(R.target[R.Type == "M"], size = N.per.type[2], replace = TRUE),
                    sample(R.target[R.Type == "R"], size = N.per.type[3], replace = TRUE))
    }
    BS <- c(BS, mean(add.this))
  }
  
  # Calculate the observed mean
  Observed <- mean(R.target) * (12.5^2 * pi) / ((1.408 * 1.056) - 0.140)
  
  # Plot the bootstrap distribution with the observed mean as a vertical line
  plot(density(BS))
  abline(v = Observed, col = "blue", lwd=2)
  
  mean.result <- mean(BS)
  sd.result <- sqrt(var(BS)) 
    
  variability <- (2 * (sd.result / mean.result) * 100)
    
  write.table(cbind("Observed" = Observed), file = CSV_file, append = TRUE, sep = ",", row.names = FALSE, col.names = TRUE)
  write.table(t(quantile(BS, c(.025, .25, .50, .75, .975))), file = CSV_file, append = TRUE, sep = ",", row.names = FALSE, col.names = c("2.5%", "25%", "50%", "75%", "97.5%"))
  write.table(cbind("Mean" = mean.result), file = CSV_file, append = TRUE, sep = ",", row.names = FALSE, col.names = TRUE)
  write.table(cbind("Std deviation" = sd.result), file = CSV_file, append = TRUE, sep = ",", row.names = FALSE, col.names = TRUE)
  write.table(cbind("Variability" = variability), file = CSV_file, append = TRUE, sep = ",", row.names = FALSE, col.names = TRUE)
  cat("\n", file = CSV_file, append = TRUE)
}
