import urllib2
import argparse
import re
import time
import socket
import os
from Queue import Queue
from sets import Set
from random import random
import datetime 
import json

# TODO: Replace [at] to @
# TODO: Connection to TOR
# TODO: Set headers like basic browsers

# Test if a string is http/https url
def isHTTPurl(urlTarget):
	if(re.match("^https?://[a-zA-Z0-9/\-_.]+", urlTarget) == None):
		msg = "%r: url must be format to http request" % urlTarget
		raise argparse.ArgumentTypeError(msg)
	return urlTarget

def isHTTPurl2(urlTarget):
	return not (re.match("^https?://[a-zA-Z0-9/\-_.]+", urlTarget) == None)

def findAllMail(text):
	mails = re.findall("([a-zA-Z0-9\-+_-]+(@| ?\[at\] ?)[a-zA-Z0-9-_.\-]+\.[a-zA-Z0-9-_\-]+)", text)
	ret = Set()
	for mail in mails:
		ret.add(mail[0])
	return ret

def findUrl(text, prot, domain, path):
	urls = re.findall("((src|href)=[\"' ]?([/a-zA-Z0-9:._-]+)[\"' ]?)|(url\([\"']?([/a-zA-Z0-9:._-]+)[\"']?\))", text)
	ret = Set()
	for groupUrl in urls:
		if(groupUrl[2]):
			url = groupUrl[2]
		elif(groupUrl[4]):
			url = groupUrl[4]
		else:
			continue
		if(url.startswith("#") and not isHTTPurl(url)):
			continue
		if(not path):
			path = "/"
		if(url.startswith("/")):
			url = prot+domain+url
		elif(not getProt(url)):
			url = prot+domain+path+url
		ret.add(url.split("#")[0])
	return ret

def getDomain(url):
	ret = re.search("https?://([a-zA-Z0-9\-_.]*[a-zA-Z0-9\-_])", url)
	if ret:
		return ret.group(1)
	else:
		return None

def getBaseUrl(url):
	ret = re.search("(https?://[a-zA-Z0-9\-_.]*[a-zA-Z0-9\-_])", url)
	if ret:
		return ret.group(1)
	else:
		return None

def getPath(url):
	ret = re.search("//[^/]+(/[/a-zA-Z0-9\-_.]*)/", url)
	if ret:
		return ret.group(1)
	else:
		return '/'

def getProt(url):
	if url.startswith("https://"):
		return "https://"
	elif url.startswith("http://"):
		return "http://"
	else:
		return None

def getSubUrl(urlFind):
	baseUrl = getBaseUrl(urlFind)
	path = getPath(urlFind)
	decompPath = path.split('/')
	for _ in range(decompPath.count('')):
		decompPath.remove('')
	ret = []
	for i in range(len(decompPath)+1):
		tmpPath = baseUrl+"/"
		for j in range(i):
			tmpPath += decompPath[j]+"/"
		ret.append(tmpPath)
	return ret

def saveContext(saveContextFilePath, queuePages, setUrlViewed, setMails, page, verbosity, filePage, fileMail, sleep, randTime, callbacksMail, callbacksPage, filesCallbackMail, filesCallbackPage, saveTime, domain, cmpt):
	saveContextFile = open(saveContextFilePath, "w")
	context = {}
	context['queuePages'] = [item for item in queuePages.queue]
	context['setUrlViewed'] = setUrlViewed
	context['options'] = []
	if(setMails != None):
		context['options'].append("mail")
		context['fileMail'] = fileMail.name
		context['setMails'] = []
		for mail in setMails:
			context['setMails'].append(mail)
		context['callbacksMail'] = []
		if(callbacksMail != None):
			for fun in callbacksMail:
				context['callbacksMail'].append(fun.__name__)
			context['filesCallbackMail'] = []
			if(filesCallbackMail != None):
				for f in filesCallbackMail:
					context['filesCallbackMail'].append(f.name)
	if(page):
		context['options'].append("page")
		context['filePage'] = filePage.name
		if(callbacksPage != None):
			context['callbacksPage'] = []
			for fun in callbacksPage:
				context['callbacksPage'].append(fun.__name__)
			context['filesCallbackPage'] = []
			if(filesCallbackPage != None):
				for f in filesCallbackPage:
					context['filesCallbackPage'].append(f.name)
	context['verbosity'] = verbosity
	context['sleep'] = sleep
	context['randTime'] = randTime
	context['saveTime'] = saveTime
	context['domain'] = domain
	context['cmpt'] = cmpt
	json.dump(context, saveContextFile)
	saveContextFile.close()

# Function wich run scraping
def crawl(url=None, setMails=None, page=False, verbosity=None, fileMail=None, filePage=None, sleep=0, randTime=False, callbacksMail=None, callbacksPage=None, filesCallbackMail=None, filesCallbackPage=None, saveContextFilePath=None, saveTime=5,  queuePages=Queue(), domain=None, setUrlViewed=None, cmpt = 0):
	
	# Initialize searching variables	
	if(url != None):
		queuePages.put(url)
		domain=getDomain(url)
		setUrlViewed = [url]

	lastSaveTime = datetime.datetime.now()

	# Print start message
	if(verbosity >= 2):
		if(page and setMails != None):
			print "SEARCH MAILS ADDRESS AND FILES TREE"
		elif(page):
			print "SEARCH FILES TREE"
		elif(setMails != None):
			print "SEARCH MAILS ADDRESS"

	# Main loop 
	# Except: KeyboardInterrupt
	#		Manual interruption of user
	try:
		while(not queuePages.empty()):
			#Save context
			if(saveContextFilePath != None 
					and saveTime != None
					and lastSaveTime + datetime.timedelta(seconds=saveTime) < datetime.datetime.now()):
				saveContext(saveContextFilePath, queuePages, setUrlViewed, setMails, page, verbosity, filePage, fileMail, sleep, randTime, callbacksMail, callbacksPage, filesCallbackMail, filesCallbackPage, saveTime, domain, cmpt)
				lastSaveTime = datetime.datetime.now()
				
			# Sleep pause
			if(randTime):
				time.sleep(sleep*1.1*(1-random()/5.0))
			else:
				time.sleep(sleep)

			url = queuePages.get()

			try:
				request = urllib2.Request(url=url,headers={'User-Agent':"Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11"})
				response = urllib2.urlopen(request)

				# Only if page is return
				if(200 <= response.getcode() and response.getcode() <= 399):
					# Print page found
					if(page):
						if(filePage != None):
							filePage.write(url+"\n")
							filePage.flush()

						# Execute callbacks to each pages found
						if(callbacksPage):
							for i in range(len(callbacksPage)):
								callbacksPage[i](url, verbosity, filesCallbackPage[i])

					# Only if return page is linked with web navigation (html, css, js)
					if('text/html' in response.info()['Content-Type'] 
						or (request.get_header('Accept') and 'text/css' in request.get_header('Accept'))
						or url.endswith('.js')):
						body = response.read()

						# Only if mails capture is enabled
						if(setMails != None):
							for mail in findAllMail(body):
								if(not mail in setMails):
									setMails.add(mail)

									# Print mails address found
									if(fileMail != None):
										fileMail.write(mail+"\n")
										fileMail.flush()
									
									# Execute callbacks to each mails addresses found
									if(callbacksMail):
										for i in range(len(callbacksPage)):
											callbacksMail[i](mail, verbosity, filesCallbackMail[i])

						# Add url to the waiting queue
						for urlFind in findUrl(body, getProt(url), getDomain(url), getPath(url)):
							if(not urlFind in setUrlViewed):
								if(isHTTPurl2(urlFind) and getDomain(urlFind).endswith(domain)):
									queuePages.put(urlFind)
									setUrlViewed.append(urlFind)
								
									# Inspect all sub directory path
									for subUrl in getSubUrl(urlFind):
										if(not subUrl in setUrlViewed):
											queuePages.put(subUrl)
											setUrlViewed.append(subUrl)
			except urllib2.HTTPError as e:
				# Print fails
				if(verbosity >= 4):
					print "Fail: \t"+url+"\t"+str(response.getcode())
			except urllib2.URLError as e:
				pass
			except socket.error as e:
				pass
			except AttributeError as e:
				print urlFind
				print getDomain(urlFind)
				raise AttributeError
			# Print advancement
			if(verbosity >= 3):
				cmpt+=1
				print "["+str(cmpt)+"/"+str(queuePages.qsize())+"]\t"+url+"\t"+str(response.getcode())
	except KeyboardInterrupt:
		print "Keyboard Interrupt"

if __name__ == "__main__":
	parser = argparse.ArgumentParser(description='Run into a web site')
	parser.add_argument("-o", "--option", help="Indicate all option to launch program. page : Return all web site pages found. mail : return all email address found. Incompatible with --file-mail and --file-page.", choices=['page', 'mail'], nargs='+')
	parser.add_argument("--file-mail", help="Indicate file where mail results will save.")
	parser.add_argument("--file-page", help="Indicate file where page results will save.")
	parser.add_argument("-fpn","--file-page-no", help="Don't save pages results in file.", action='store_true')
	parser.add_argument("-fmn","--file-mail-no", help="Don't save mails results in file.", action='store_true')
	parser.add_argument("-s", "--sleep", help="Indicate time in milliseconds between to request", type=int)
	parser.add_argument("-v", "--verbosity", help="Indicate the verbosity level to the execution", type=int, choices=[1,2,3,4])
	parser.add_argument("-r", "--rand", help='To more discretion time is randomize -/+10 percent each time.', action='store_true')
	parser.add_argument("-S", "--save", help='Save context in execution to load its next launch if -l/--load is set. Set the time between two save (minutes).', type=int)
	parser.add_argument("-l", "--load", help='Loas context save before', action='store_true')
	parser.add_argument("url", help="Url to file which is  testing", type=isHTTPurl)
	args = parser.parse_args()
	
	page = False
	setMails = None
	filePage = None
	fileMail = None
	filesCallbackPage = None
	filesCallbacksMail = None
	randTime = False
	verbosity = 1
	sleep = 0
	saveContextFilePath = ".bulldozer/savedContext.save"
	saveTime = None
	
	if(args.load):
		callbacksPage = None
		callbacksMail = None

		fileContext = open(saveContextFilePath, "r")
		context = json.load(fileContext)
		fileContext.close()
		if('mail' in context['options']):
			setMails=Set()
			for mail in context['setMails']:
				setMails.add(mail)
			callbacksMail = []
			for callback in context['callbacksMail']:
				callbacksMail.append(globals()[callback])
			fileMail = open(context['fileMail'], 'a')
			filesCallbackMail = []
			if('filesCallbackMail' in context.keys()):
				for name in context['filesCallbackMail']:
					filesCallbackMail.append(name)
		if('page' in context['options']):
			page = True
			callbacksPage = Set()
			for callback in context['callbacksPage']:
				callbacksPage.append(globals()[callback])
			filePage = open(context['filePage'], 'a')
			filesCallbackPage = []
			if('filesCallbackPage' in context.keys()):
				for name in context['filesCallbackPage']:
					filesCallbackPage.append(name)
		queuePages = Queue()
		for page in context['queuePages']:
			queuePages.put(page)

		crawl(setMails=setMails, page=page, verbosity=context['verbosity'], fileMail=fileMail, filePage=filePage, sleep=context['sleep'], randTime=context['randTime'], callbacksMail=callbacksMail, callbacksPage=callbacksPage, filesCallbackMail=filesCallbackMail, filesCallbackPage=filesCallbackPage, saveContextFilePath=saveContextFilePath, saveTime=context['saveTime'],  queuePages=queuePages, domain=context['domain'], setUrlViewed=context['setUrlViewed'], cmpt=context['cmpt'])
	else:
		#Stop if no option are set
		if(args.option == None):
			print "ERROR: You must choose some options"
			exit(-1);

		#Initiate save session files
		if(args.save != None):
			saveTime = args.save
			try:
				os.mkdir(".bulldozer")
			except OSError as e:
				pass

		#Mail option
		if("mail" in args.option):
			setMails = Set()

		#Page option
		if("page" in args.option):
			page = True

		#File mail
		if(args.file_mail_no or setMails == None):
			fileMail = None
			if(args.file_mail):
				print "WARNING: Don't use -fmn with --file-mail"
		else:
			if(args.file_mail):
				fileMail = open(args.file_mail+".txt","w")
			else:
				fileMail = open("resultMail.txt","w")

		#Page mail
		if(args.file_page_no or not page):
			filePage = None
			if(args.file_page):
				print "WARNING: Don't use -fpn with --file-page"
		else:
			if(args.file_page):
				filePage = open(args.file_page+".txt","w")
			else:
				filePage = open("resultPage.txt","w")

		#Verbosity
		if(args.verbosity):
			verbosity = args.verbosity

		#Sleep time
		if(args.sleep):
			sleep = args.sleep/1000.0
			if(args.rand):
				randTime = True
		else:
			if(args.rand):
				print "WARNING: -r/--rand needs -s/--sleep"
		
		crawl(args.url, setMails=setMails, page=page, verbosity=verbosity, filePage=filePage, fileMail=fileMail, sleep=sleep, randTime=randTime, saveContextFilePath=saveContextFilePath, saveTime=saveTime)
