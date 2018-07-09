package main

import (
	"github.com/brutella/dnssd"
	"context"
	"fmt"
	"os"
	"os/signal"
	"time"
	"github.com/gin-gonic/gin"
	"net/http"
	"net"
	"os/exec"
	"log"
	"github.com/spf13/viper"
)

var timeFormat = "15:04:05.000"
var command = "rundll32.exe"
var cParam = "url.dll,FileProtocolHandler"

func main() {
	viper.SetConfigName("beautiful-testers-config")
	viper.AddConfigPath(".")
	viper.SetConfigType("toml")
	if viper.ReadInConfig() != nil {
		panic(fmt.Errorf("读取不到beautiful-testers-config"))
	}

	serverName := viper.GetString("name")
	p := viper.GetInt("port")

	addrs, err := net.InterfaceAddrs()
	var host = ""
	if err != nil {
		fmt.Println(err)
		panic(err)
	}

	for _, address := range addrs {
		if ipnet, ok := address.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
			if ipnet.IP.To4() != nil {
				host = ipnet.IP.String()
			}

		}
	}
	log.Println(fmt.Sprintf("host---> %s:%d", host, p))
	go serv(p)

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	if resp, err := dnssd.NewResponder(); err != nil {
		fmt.Println(err)
	} else {
		srv := dnssd.NewService(serverName, "_http._tcp.", "local.", host, nil, p)
		go func() {
			stop := make(chan os.Signal, 1)
			signal.Notify(stop, os.Interrupt)

			select {
			case <-stop:
				cancel()
			}
		}()

		go func() {
			time.Sleep(100 * time.Millisecond)
			handle, err := resp.Add(srv)
			if err != nil {
				fmt.Println(err)
			} else {
				fmt.Printf("%s	Got a reply for service %s: Name now registered and active\n", time.Now().Format(timeFormat), handle.Service().ServiceInstanceName())
			}
		}()
		err = resp.Respond(ctx)

		if err != nil {
			fmt.Println(err)
		}
	}
}

func serv(port int)  {
	router := gin.Default()

	router.Static("/", "./public")
	router.POST("/upload", func(c *gin.Context) {


		file, err := c.FormFile("file")
		if err != nil {
			c.String(http.StatusBadRequest, fmt.Sprintf("get form err: %s", err.Error()))
			return
		}

		if err := c.SaveUploadedFile(file, file.Filename); err != nil {
			c.String(http.StatusBadRequest, fmt.Sprintf("upload file err: %s", err.Error()))


			return
		}else {
			log.Println(file.Filename)
			cmd := exec.Command("cmd", "/C start "+file.Filename)
			if err := cmd.Run(); err != nil {
				fmt.Println("Error: ", err)
			}
		}

		c.String(http.StatusOK, fmt.Sprintf("File %s uploaded successfully"))
	})
	router.Run(fmt.Sprintf(":%d", port))
}