#!/bin/bash

file_name="test.csv"

echo "Time,Floor,Floor Button,Car Button" > "$file_name"

add30seconds() {
	local incrementby=$1


	new_time=$(date -d "now + 20 seconds + $incrementby seconds" "+%H:%M:%S:%S")

	echo "$new_time"
}

upordown() {
	random_number=$((RANDOM % 2))
	if [ $random_number -eq 0 ]; then
		direction="Down"
	else
		direction="Up"
	fi
	echo "$direction"
}



# Start time as current time + 1 minute

# Generate rows with incremented times
for ((i=0; i<50; i+=1)); do
	incremented_time=$(add30seconds "$((1 * i))")
	direction=$(upordown)

	echo "$incremented_time,$((i%9 + 1)),$(upordown),$(((i+3)%9 + 1))" >> "$file_name"
done

echo "CSV file '$file_name' generated successfully."

