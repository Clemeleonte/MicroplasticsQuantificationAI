import os
import subprocess
# Get the current working directory and print it to the console
cwd = os.getcwd()
print(cwd)

# Create a list of directories in the current working directory
directories = [f for f in os.listdir(cwd) if os.path.isdir(os.path.join(cwd, f))]
pint(directories)

# For each directory in the list of directories
for directory in directories:
    # Set the working directory to the current directory in the loop
    work_dir = os.path.join(cwd, directory)
    # Set the h5_files directory to a subdirectory of the current directory called "h5_files"
    h5_dir = os.path.join(work_dir, "h5_files")
    
    # Try to create the h5_files subdirectory, but do nothing if it already exists
    try:
        os.mkdir(h5_dir)
    except FileExistsError:
        pass
    
    # Create a list of all .h5 files in the current directory
    h5_files = [os.path.join(work_dir, f) for f in os.listdir(work_dir) if f.endswith(".h5")]
    # Join the list of .h5 files into a string separated by spaces
    proc_files = " ".join(h5_files)
    
    # Set the ilastik command to be run in the next line, including input and output directories and file formats
    ilastik_cmd = f'C:\\Program Files\\ilastik-1.4.0rc6\\ilastik.exe --headless --project={cwd}\\testing.ilp --output_format=tif --export_source="Simple Segmentation" {proc_files}'
     # Run the ilastik command as a subprocess
    subprocess.call(ilastik_cmd)

    # Loop through each h5 file in the current directory and move it to the h5_files subdirectory
    for h5_file in h5_files:
        print(h5_file)
        os.system(f'MOVE /Y {h5_file} {h5_dir}')
