echo "Creating gradle.properties file"
FILE="../gradle.properties"

touch $FILE
echo "MapboxAPIKey=\"$MAPBOX_API\"" >> $FILE
echo "AppCenterKey=\"$APPCENTER\"" >> $FILE

