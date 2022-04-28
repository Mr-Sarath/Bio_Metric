
# Welcome to Bio Metric!
Your finger enough for safety!!  


Step 1. Add the JitPack repository to your settings.gradle file

    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  
 
Step 2. Add the dependency in build.gradle(module:app)
 
    dependencies {
	           implementation 'androidx.biometric:biometric:1.1.0'
	 }
