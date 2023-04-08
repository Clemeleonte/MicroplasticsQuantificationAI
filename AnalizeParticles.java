// ImageJ macro to count and size microplastics
// Saves a copy of the binarized mask in a new subfolder
// Saves in the sample folder a .csv result table for each image 

// Define input and output directories
inputDir = getDirectory("Choose input directory");
outputDir = getDirectory("Choose output directory");
backslash = File.separator;

// Iterate through all subdirectories in the input directory
subDirs = getFileList(inputDir);
for (i = 0; i < subDirs.length; i++) {
    if (endsWith(subDirs[i], backslash)) {
        subDir = substring(subDirs[i], 0, subDirs[i].length - 1);
        print("Processing directory: " + subDir);
        processSubDir(inputDir + subDir + backslash, outputDir + subDir + backslash);
    } 
}

// Exit ImageJ
run("Quit");

// Function to process a single subdirectory
function processSubDir(inputSubDir, outputSubDir) {
    
    // Check that the folder contain TIFFs 
    if (nImages==0) {
        print("Folder is empty");
        break;
    }

    // Define subdirectories for processed TIFFs and outlines
    processedTifDir = outputSubDir + "Processed_tif" + backslash;
    outlinesDir = outputSubDir + "Outlines" + backslash;

    // Create subdirectories if they don't already exist
    File.makeDirectory(processedTifDir);
    File.makeDirectory(outlinesDir);

    // Iterate through all TIFF files in the input subdirectory
    tifFiles = getFileList(inputSubDir);
    for (j = 0; j < tifFiles.length; j++) {
        if (endsWith(tifFiles[j], ".tif")) {
            tifFile = inputSubDir + backslash + tifFiles[j];
            name = substring(tifFiles[j], 0, tifFiles[j].length - 4);

            // Open the TIFF file and save a copy to the processed TIFF directory
            open(tifFile);
            saveAs("tif", processedTifDir + name + ".tif");

            // Apply image processing steps
            setThreshold(255, 255);
            setOption("BlackBackground", false);
            run("Convert to Mask");
            run("Watershed");
            run("Remove Outliers...", "radius=10 threshold=50 which=Dark");
            run("Set Scale...", "distance=1 known=0.55 unit=um global");
            run("Analyze Particles...", "size=78.54-25000 show=Outlines display clear");

            // Save the outlines image to the outlines directory
            drawingOf = "Drawing of " + tifFiles[j];
            selectWindow(drawingOf);
            saveAs(drawingOf, outlinesDir + name + "_outlines.tif");
            close();

            // Modify and save the results table
            density = 0.00000000094;
            macroTable=""+
            "sumWeight = 0.0;" +
            "for (row=1; row < Table.size; row++){" +
            "  major = Table.get(\"Major\",row);" +
            "  minor = Table.get(\"Minor\",row);" +
            "  weight = minor * minor * major * 1.0/9.0 * PI * "+density+";" +
            "  Table.set(\"Weight\", row, weight);" +
            "  sumWeight+=weight;" +
            "}" +
            "Table.set(\"SumWeight\", 1, sumWeight);" +
            "Table.update;";
            tableResults = "Results";
            selectWindow(tableResults);
            Table.applyMacro(macroTable);
            saveAs(tableResults, outputSubDir + name + "_Results.csv");
            close();

            // Delete the original TIFF file
            File.delete(tifFile);
        }
    }
    print("Successfully processed sample " + inputSubDir);
}

