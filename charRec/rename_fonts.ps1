cd C:\Users\MurG\source\repos\charRec\Fonts

if($?) {
	$files_counter = 70
	$files=get-childitem
	foreach ($file in $files) {
		Rename-Item $file.name ($files_counter)
		
		cd C:\Users\MurG\source\repos\charRec\Fonts\$files_counter
		
		if($?) {
			$folder_files=get-childitem
			$folder_files_counter = 0
			
			foreach ($folder_file in $folder_files) {
				
				$file_name = "$($folder_files_counter).png"
				Rename-Item $folder_file.name ($file_name)
				
				$folder_files_counter = $folder_files_counter + 1
			}
			
			cd ..
		}
		
		$files_counter = $files_counter + 1
		
	}
}