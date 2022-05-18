import numpy as np
import csv
import pandas as pd
from pyaxis import pyaxis


def load(csv_file="../data/kd2019.csv", delimiter=";") -> np.array:
  with open(csv_file, 'rt', encoding="cp1250") as csvfile:
    reader = csv.reader(csvfile, delimiter=delimiter)
    head = next(reader)
    none_handler = lambda i : i or None
    main_data = np.array([none_handler(i) for i in [row for row in reader]])
  csvfile.close()
  return main_data

def load_pd(csv_file="../data/kd2019.csv", delimiter=";") -> pd.DataFrame:
  return pd.read_csv(csv_file, encoding="cp1250", delimiter=delimiter)

def load_px(px_file="../data/kd_sankcije.px") -> pd.DataFrame:
  px = pyaxis.parse(uri = px_file , encoding = 'cp1250')
  #store data as pandas dataframe
  return px['DATA']
